package lt.gama.model.type;

import lt.gama.model.sql.documents.DoubleEntrySql;

public class DocumentDoubleEntry<E>{
    private E document;
    private DoubleEntrySql doubleEntry;


    @SuppressWarnings("unused")
    protected DocumentDoubleEntry() {}

    public DocumentDoubleEntry(E document) {
        this.document = document;
        this.doubleEntry = null;
    }

    public DocumentDoubleEntry(E document, DoubleEntrySql doubleEntry) {
        this.document = document;
        this.doubleEntry = doubleEntry;
    }

    public E getDocument() {
        return document;
    }

    public DoubleEntrySql getDoubleEntry() {
        return doubleEntry;
    }
}
