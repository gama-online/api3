package lt.gama.impexp;

import lt.gama.model.i.ICompany;

/**
 * Gama
 * Created by valdas on 15-06-10.
 */
public interface LinkBase<E extends ICompany> {

    E resolve(E document);

    void finish(long documentId);
}
