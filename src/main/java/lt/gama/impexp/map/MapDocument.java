package lt.gama.impexp.map;

import lt.gama.impexp.DocumentImport;
import lt.gama.impexp.MapBase;

/**
 * Gama
 * Created by valdas on 15-06-08.
 */
public class MapDocument extends MapBase<DocumentImport> {

    @Override
    public Class<DocumentImport> getEntityClass() {
        return DocumentImport.class;
    }

}
