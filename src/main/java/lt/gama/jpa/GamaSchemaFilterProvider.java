package lt.gama.jpa;

import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

public class GamaSchemaFilterProvider implements SchemaFilterProvider {

    static final SchemaFilter schemaFilter = new SchemaFilter() {

        @Override
        public boolean includeNamespace(Namespace namespace) {
            return true;
        }

        @Override
        public boolean includeTable(Table table) {
            return !table.getName().toLowerCase().startsWith("rep");
        }

        @Override
        public boolean includeSequence(Sequence sequence) {
            return true;
        }
    };

    @Override
    public SchemaFilter getCreateFilter() {
        return schemaFilter;
    }

    @Override
    public SchemaFilter getDropFilter() {
        return schemaFilter;
    }

    @Override
    public SchemaFilter getTruncatorFilter() {
        return schemaFilter;
    }

    @Override
    public SchemaFilter getMigrateFilter() {
        return schemaFilter;
    }

    @Override
    public SchemaFilter getValidateFilter() {
        return schemaFilter;
    }
}
