package test.io.netty;

import org.apache.avro.Schema;

public class RegistryItem {

    private final String subject;
    private final Long id;
    private final Integer version;
    private final Schema schema;

    public RegistryItem(final String subject, final Long id, final Integer version, final Schema schema) {
        this.subject = subject;
        this.id = id;
        this.version = version;
        this.schema = schema;
    }

    public String getSubject() {
        return this.subject;
    }

    public Long getId() {
        return this.id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public String toString() {
        return "RegistryItem [subject=" + this.subject + ", id=" + this.id + ", version=" + this.version + ", schema=" + this.schema + "]";
    }
}
