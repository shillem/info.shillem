package info.shillem.dao.lang;

public class DaoResolutionException extends DaoException {

    private static final long serialVersionUID = 1L;

    public DaoResolutionException(String url) {
        super(
                String.format("The resource with url %s is not resolvable", url),
                DaoErrorCode.UNRESOLVABLE_NOTES_URL);
    }

}
