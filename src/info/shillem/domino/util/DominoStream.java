package info.shillem.domino.util;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import info.shillem.util.Unthrow.ThrowableFunction;
import info.shillem.util.Unthrow.ThrowableSupplier;
import lotus.domino.Base;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class DominoStream {

    private DominoStream() {

    }

    public static Stream<Document> stream(DocumentCollection coll) {
        return stream(coll, ViewNavigation.FORWARD);
    }

    public static Stream<Document> stream(DocumentCollection coll, ViewNavigation order) {
        Objects.requireNonNull(coll, "Collection cannot be null");
        Objects.requireNonNull(order, "Order cannot be null");

        ThrowableSupplier<Document> starter;
        ThrowableFunction<Document, Document> advancer;

        if (order == ViewNavigation.FORWARD) {
            starter = coll::getFirstDocument;
            advancer = coll::getNextDocument;
        } else {
            starter = coll::getLastDocument;
            advancer = coll::getPrevDocument;
        }

        return stream(new DominoIterator<>(starter, advancer));
    }

    private static <T extends Base> Stream<T> stream(DominoIterator<T> iterator) {
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .onClose(iterator::close);
    }

    public static Stream<Document> stream(View view, ViewNavigation order) {
        Objects.requireNonNull(view, "View cannot be null");
        Objects.requireNonNull(order, "Order cannot be null");

        ThrowableSupplier<Document> starter;
        ThrowableFunction<Document, Document> advancer;

        if (order == ViewNavigation.FORWARD) {
            starter = view::getFirstDocument;
            advancer = view::getNextDocument;
        } else {
            starter = view::getLastDocument;
            advancer = view::getPrevDocument;
        }

        return stream(new DominoIterator<>(starter, advancer));
    }

    public static Stream<Document> stream(View view) {
        return stream(view, ViewNavigation.FORWARD);
    }

    public static Stream<ViewEntry> stream(ViewEntryCollection coll) {
        return stream(coll, ViewNavigation.FORWARD);
    }

    public static Stream<ViewEntry> stream(
            ViewEntryCollection coll, ViewNavigation order) {
        Objects.requireNonNull(coll, "View entry collection cannot be null");
        Objects.requireNonNull(order, "Order cannot be null");

        ThrowableSupplier<ViewEntry> starter;
        ThrowableFunction<ViewEntry, ViewEntry> advancer;

        if (order == ViewNavigation.FORWARD) {
            starter = coll::getFirstEntry;
            advancer = coll::getNextEntry;
        } else {
            starter = coll::getLastEntry;
            advancer = coll::getPrevEntry;
        }

        return stream(new DominoIterator<>(starter, advancer));
    }

    public static Stream<ViewEntry> stream(ViewNavigator nav) {
        return stream(nav, ViewEntryFilter.ANY, ViewNavigation.FORWARD);
    }

    public static Stream<ViewEntry> stream(ViewNavigator nav, ViewEntryFilter filter) {
        return stream(nav, filter, ViewNavigation.FORWARD);
    }

    public static Stream<ViewEntry> stream(
            ViewNavigator nav, ViewEntryFilter filter, ViewNavigation order) {
        Objects.requireNonNull(nav, "View navigator cannot be null");
        Objects.requireNonNull(filter, "View entry filter cannot be null");
        Objects.requireNonNull(order, "Order cannot be null");

        ThrowableSupplier<ViewEntry> starter = null;
        ThrowableFunction<ViewEntry, ViewEntry> advancer = null;

        if (order == ViewNavigation.FORWARD) {
            switch (filter) {
            case ANY:
                starter = nav::getFirst;
                advancer = nav::getNext;
                break;
            case CATEGORY:
                starter = nav::getFirst;
                starter = () -> {
                    if (!nav.gotoFirst()) {
                        return null;
                    }

                    ViewEntry entry = nav.getCurrent();

                    if (entry.isCategory()) {
                        return entry;
                    }

                    DominoUtil.recycle(entry);

                    return nav.getNextCategory();
                };
                advancer = (entry) -> nav.getNextCategory();
                break;
            case DOCUMENT:
                starter = () -> nav.gotoFirstDocument() ? nav.getCurrent() : null;
                advancer = (entry) -> nav.getNextDocument();
                break;
            case SIBLING:
                starter = nav::getFirst;
                advancer = nav::getNextSibling;
                break;
            }
        } else {
            switch (filter) {
            case ANY:
                starter = nav::getLast;
                advancer = nav::getPrev;
                break;
            case CATEGORY:
                starter = () -> {
                    if (!nav.gotoLast()) {
                        return null;
                    }

                    ViewEntry entry = nav.getCurrent();

                    if (entry.isCategory()) {
                        return entry;
                    }

                    DominoUtil.recycle(entry);

                    return nav.getPrevCategory();
                };
                advancer = (entry) -> nav.getPrevCategory();
                break;
            case DOCUMENT:
                starter = () -> nav.gotoLastDocument() ? nav.getCurrent() : null;
                advancer = (entry) -> nav.getPrevDocument();
                break;
            case SIBLING:
                starter = nav::getLast;
                advancer = nav::getPrevSibling;
                break;
            }
        }

        return stream(new DominoIterator<>(starter, advancer));
    }

}
