package info.shillem.util.dots;

import info.shillem.domino.util.DominoUtil;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;

public abstract class AbstractConfigurationInitializer
        extends com.ibm.dots.preferences.AbstractConfigurationInitializer {

    @Override
    protected void initializeDefaultConfigurationParameters(Document doc) throws NotesException {
        Item item = null;

        try {
            item = doc.replaceItemValue("pref_NOTES_URL", doc.getNotesURL());
        } finally {
            DominoUtil.recycle(item);
        }
    }

}
