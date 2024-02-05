package org.quelea.utils;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitCollectionTableNameSource;
import org.hibernate.boot.model.naming.ImplicitConstraintNameSource;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitIndexNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;
import org.hibernate.boot.model.naming.NamingHelper;

/**
 * Custom implicit naming strategy for hibernate 5.
 * <p/>
 * Necessary because the new hibernate version introduced new semantics and new interfaces and configuration of
 * the naming strategy which is responsible for the determination of the names of database objects (tables, fields,
 * constraints, indices and others) that are not explicitly named.
 * <p/>
 * Unfortunately the new naming strategies do not replicate the exact same behaviour of each of the
 * old ones - because the new strategies use more context knowledge than the old ones which
 * ignored some of it.
 * <p/>
 * The new naming strategy that is closest to the EJB3NamingStrategy which we we used before is the
 * ImplicitNamingStrategyLegacyJpaImpl (its javadoc says it corresponds "roughly" to it) which is what we use as
 * a base for further customization in order to work around some remaining differences.
 */
public class EJB3ImplicitNamingStrategy extends ImplicitNamingStrategyLegacyJpaImpl {

    /**
     * the original strategy would use the table name which often is set explicitly (e.g. "USERS" for the "User" class)
     * but we need the original class name which is what EJB3NamingStrategy seemingly was using ("USER" instead of "USERS").
     * <p/>
     * Following code is copy of superclass which only changes this detail.
     */
    @Override
    public Identifier determineCollectionTableName(ImplicitCollectionTableNameSource source) {
        String owningClassName = source.getOwningEntityNaming().getClassName();
        String owningTableName = owningClassName.substring(owningClassName.lastIndexOf('.') + 1);
        Identifier identifier = toIdentifier(
                owningTableName + "_" + transformAttributePath(source.getOwningAttributePath()),
                source.getBuildingContext()
        );
        if (source.getOwningPhysicalTableName().isQuoted()) {
            identifier = Identifier.quote(identifier);
        }
        return identifier;

    }

    /**
     * Same as above, base new identifier on class name instead of declared table name. Again copied
     * and changed code from superclass.
     */
    @Override
    public Identifier determineJoinColumnName(ImplicitJoinColumnNameSource source) {
        final String name;

        if (source.getNature() == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION
                || source.getAttributePath() == null) {

            String referencedClassName = source.getEntityNaming().getClassName();
            String referencedTableName = referencedClassName.substring(referencedClassName.lastIndexOf('.') + 1);

            name = referencedTableName
                    + '_'
                    + source.getReferencedColumnName().getText();
        } else {
            name = transformAttributePath(source.getAttributePath())
                    + '_'
                    + source.getReferencedColumnName().getText();
        }

        return toIdentifier(name, source.getBuildingContext());
    }

    /**
     * Replicate hibernate 4.3's constraint naming strategy in EJBNamingStrategy, which differs in the following
     * ways:
     * - include underscore between prefix and hash value
     * - prefix unique indices also with UK_ instead of IDX_
     * - do not include the referenced table name in the hash value generation for foreign keys (do not use
     * generateHashedFkName method but instead stay with generateHashedConstraintName).
     */
    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        return generateHashedConstraintName(source, "FK_");
    }

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        return generateHashedConstraintName(source, "UK_");
    }

    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        return generateHashedConstraintName(source, "UK_");
    }

    private Identifier generateHashedConstraintName(ImplicitConstraintNameSource source, String prefix) {
        return toIdentifier(
                NamingHelper.INSTANCE.generateHashedConstraintName(
                        prefix,
                        source.getTableName(),
                        source.getColumnNames()
                ),
                source.getBuildingContext()
        );
    }

}