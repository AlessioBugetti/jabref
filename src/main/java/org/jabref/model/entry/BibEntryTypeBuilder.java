package org.jabref.model.entry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jabref.model.entry.field.BibField;
import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.FieldPriority;
import org.jabref.model.entry.field.OrFields;
import org.jabref.model.entry.types.EntryType;
import org.jabref.model.entry.types.StandardEntryType;

import com.google.common.collect.Streams;

public class BibEntryTypeBuilder {

    private EntryType type = StandardEntryType.Misc;

    private SequencedSet<OrFields> requiredFields = new LinkedHashSet<>();
    private SequencedSet<BibField> optionalFields = new LinkedHashSet<>();

    private Set<Field> seenFields = new HashSet<>();

    public BibEntryTypeBuilder withType(EntryType type) {
        this.type = type;
        return this;
    }

    public BibEntryTypeBuilder withImportantFields(SequencedSet<Field> newFields) {
        List<Field> containedFields = containedInSeenFields(newFields);
        if (!containedFields.isEmpty()) {
            throw new IllegalArgumentException("Fields " + containedFields + " already added");
        }
        this.seenFields.addAll(newFields);
        this.optionalFields = Streams.concat(optionalFields.stream(), newFields.stream().map(field -> new BibField(field, FieldPriority.IMPORTANT)))
                                     .collect(Collectors.toCollection(LinkedHashSet::new));
        return this;
    }

    public BibEntryTypeBuilder withImportantFields(Field... newFields) {
        return withImportantFields(Arrays.stream(newFields).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    public BibEntryTypeBuilder withDetailFields(SequencedCollection<Field> newFields) {
        List<Field> containedFields = containedInSeenFields(newFields);
        if (!containedFields.isEmpty()) {
            throw new IllegalArgumentException("Fields " + containedFields + " already added");
        }
        this.seenFields.addAll(newFields);
        this.optionalFields = Streams.concat(optionalFields.stream(), newFields.stream().map(field -> new BibField(field, FieldPriority.DETAIL)))
                                     .collect(Collectors.toCollection(LinkedHashSet::new));
        return this;
    }

    public BibEntryTypeBuilder withDetailFields(Field... fields) {
        return withDetailFields(Arrays.asList(fields));
    }

    public BibEntryTypeBuilder withRequiredFields(SequencedSet<OrFields> requiredFields) {
        return addRequiredFields(requiredFields);
    }

    public BibEntryTypeBuilder addRequiredFields(SequencedSet<OrFields> requiredFields) {
        Set<Field> fieldsToAdd = requiredFields.stream().map(OrFields::getFields).flatMap(Set::stream).collect(Collectors.toSet());
        List<Field> containedFields = containedInSeenFields(fieldsToAdd);
        if (!containedFields.isEmpty()) {
            throw new IllegalArgumentException("Fields " + containedFields + " already added");
        }
        this.seenFields.addAll(fieldsToAdd);
        this.requiredFields.addAll(requiredFields);
        return this;
    }

    public BibEntryTypeBuilder addRequiredFields(OrFields... requiredFields) {
        return addRequiredFields(Arrays.asList(requiredFields).stream().collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    public BibEntryTypeBuilder addRequiredFields(Field... requiredFields) {
        return addRequiredFields(Arrays.stream(requiredFields).map(OrFields::new).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    public BibEntryTypeBuilder withRequiredFields(Field... requiredFields) {
        return addRequiredFields(requiredFields);
    }

    public BibEntryTypeBuilder withRequiredFields(OrFields first, Field... requiredFields) {
        return addRequiredFields(Stream.concat(Stream.of(first), Arrays.stream(requiredFields).map(OrFields::new)).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    public BibEntryTypeBuilder withRequiredFields(SequencedSet<OrFields> first, Field... requiredFields) {
        return addRequiredFields(Stream.concat(first.stream(), Arrays.stream(requiredFields).map(OrFields::new)).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    public BibEntryType build() {
        // Treat required fields as important ones
        Stream<BibField> requiredAsImportant = requiredFields.stream()
                .map(OrFields::getFields)
                .flatMap(Set::stream)
                .map(field -> new BibField(field, FieldPriority.IMPORTANT));
        SequencedSet<BibField> allFields = Stream.concat(optionalFields.stream(), requiredAsImportant).collect(Collectors.toCollection(LinkedHashSet::new));
        return new BibEntryType(type, allFields, requiredFields);
    }

    private List<Field> containedInSeenFields(Collection<Field> fields) {
        return fields.stream().filter(seenFields::contains).toList();
    }
}
