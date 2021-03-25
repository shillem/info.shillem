package info.shillem.domino.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import info.shillem.dao.Query;
import info.shillem.dao.lang.DaoQueryException;
import info.shillem.domino.util.DominoLoop.OptionTotal;
import info.shillem.domino.util.DominoLoop.Options;
import info.shillem.domino.util.DominoLoop.OptionsDocument;
import info.shillem.domino.util.DominoLoop.OptionsViewEntry;
import info.shillem.domino.util.DominoLoop.Result;
import info.shillem.dto.BaseField;
import info.shillem.util.OrderOperator;
import info.shillem.util.Unthrow.ThrowableFunction;
import lotus.domino.Base;
import lotus.domino.NotesError;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class VwWalker<E extends Enum<E> & BaseField> {

    public class Parameters {
        public final Query<E> query;
        public final Function<E, String> namer;
        public final View view;

        private Parameters(Query<E> query, Function<E, String> namer, View view) {
            this.query = query;
            this.namer = namer;
            this.view = view;
        }

    }

    private final String notesUrlPrefix;
    private final VwPath path;
    private final DominoSilo silo;

    private Map<Query.Type, ThrowableFunction<Parameters, ? extends Base>> influencers;

    public VwWalker(VwPath path, DominoSilo silo) throws NotesException {
        this.path = path;
        this.silo = silo;

        notesUrlPrefix = String.format("notes://%s/%s/0/",
                silo.getDbPath().getServerNameAsUrl(),
                silo.getDatabase().getReplicaID());
    }

    public VwWalker<E> allowDefaultFilterQuery(Supplier<NavType> type, Supplier<VwMatch> match) {
        return allowFilterQuery((params) -> {
            Vector<Object> keys = params.query.getFilters()
                    .values()
                    .stream()
                    .collect(Collectors.toCollection(Vector::new));

            NavType nt = type.get();

            switch (nt) {
            case CATEGORY:
                return params.view.createViewNavFromCategory((String) keys.get(0));
            case KEY:
                return params.view.createViewNavFromKey(keys, match.get().asBoolean());
            default:
                throw new UnsupportedOperationException(
                        nt.name().concat(" filter is not supported"));
            }
        });
    }

    public VwWalker<E> allowDefaultFlatQuery() {
        return allowFlatQuery((params) -> params.view.createViewNav());
    }

    public VwWalker<E> allowDefaultSearchQuery() {
        return allowSearchQuery((params) -> {
            String syntax = new FtSearchQuery<>(params.query).withNamer(params.namer).output();

            try {
                params.view.FTSearchSorted(syntax, params.query.getLimit());

                return params.view.getAllEntries();
            } catch (NotesException e) {
                if (e.id == NotesError.NOTES_ERR_NOT_IMPLEMENTED
                        || !e.text.toLowerCase().contains("query")) {
                    throw new RuntimeException(e);
                }

                throw DaoQueryException.asInvalid(syntax);
            }
        });
    }

    public VwWalker<E> allowFilterQuery(ThrowableFunction<Parameters, ViewNavigator> fn) {
        return allowQuery(Query.Type.FILTER, fn);
    }

    public VwWalker<E> allowFlatQuery(ThrowableFunction<Parameters, ViewNavigator> fn) {
        return allowQuery(Query.Type.FLAT, fn);
    }

    private VwWalker<E> allowQuery(
            Query.Type type,
            ThrowableFunction<Parameters, ? extends Base> fn) {
        getInfluencers().put(type, fn);

        return this;
    }

    public VwWalker<E> allowSearchQuery(ThrowableFunction<Parameters, ViewEntryCollection> fn) {
        return allowQuery(Query.Type.SEARCH, fn);
    }

    public String getDatabaseUrl(ViewEntry entry) throws NotesException {
        return notesUrlPrefix.concat(entry.getUniversalID());
    }

    private Map<Query.Type, ThrowableFunction<Parameters, ? extends Base>> getInfluencers() {
        if (influencers == null) {
            influencers = new HashMap<>();
        }

        return influencers;
    }

    public <R> Result<R> getResult(
            Query<E> query,
            Consumer<OptionsViewEntry<R>> optioner,
            Function<E, String> namer)
            throws NotesException {
        Function<Parameters, ? extends Base> influencer = getInfluencers().get(query.getType());

        if (influencer == null) {
            throw query.unsupported();
        }

        Parameters params = new Parameters(
                query,
                namer,
                getView(VwAccessPolicy.valueOf(query.getOptions())));

        if (!query.getSorters().isEmpty()) {
            if (query.getSorters().size() > 1) {
                throw query.unsupported("Query does not support more than 1 sorter");
            }

            Map.Entry<E, OrderOperator> sorter = query.getSorters()
                    .entrySet()
                    .iterator()
                    .next();

            params.view.resortView(namer.apply(sorter.getKey()), sorter.getValue().asBoolean());
        }

        Base base = influencer.apply(params);

        OptionsViewEntry<R> options = newOptionsViewEntry(query);

        optioner.accept(options);

        Result<R> result;

        try {
            if (base instanceof ViewEntryCollection) {
                result = DominoLoop.read((ViewEntryCollection) base, options);
            } else {
                result = DominoLoop.read((ViewNavigator) base, options);
            }
        } finally {
            DominoUtil.recycle(base);
        }

        query.setSummary(result.getSummary());

        return result;
    }

    public View getView(VwAccessPolicy accessPolicy) throws NotesException {
        return silo.getView(path, accessPolicy);
    }

    public int indexOfColumn(String columnName) throws NotesException {
        return silo.getViewColumnNames(path).indexOf(columnName);
    }

    public static <R> OptionsDocument<R> newOptionsDocument(Query<?> query) {
        OptionsDocument<R> options = new OptionsDocument<>();

        setOptions(options, query);

        return options;
    }

    public static <R> OptionsViewEntry<R> newOptionsViewEntry(Query<?> query) {
        OptionsViewEntry<R> options = new OptionsViewEntry<>();

        setOptions(options, query);

        return options;
    }

    private static void setOptions(Options<?, ?> options, Query<?> query) {
        options.setLimit(query.getLimit());
        options.setOffset(query.getOffset());

        if (query.containsOption("FETCH_TOTAL_ONLY")) {
            options.setTotal(OptionTotal.READ_ONLY);
        } else if (query.containsOption("FETCH_TOTAL")) {
            options.setTotal(OptionTotal.READ);
        }
    }

}
