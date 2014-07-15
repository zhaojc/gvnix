package org.gvnix.addon.field.geo;

/**
 * Example of an enum used for tab-completion of properties.
 * 
 * @since 1.1.1
 */
public enum FieldGeoTypes {

    POINT("com.vividsolutions.jts.geom.Point"), COORDINATE(
            "com.vividsolutions.jts.geom.Coordinate"), GEOMETRY(
            "com.vividsolutions.jts.geom.Geometry"), POLYGON(
            "com.vividsolutions.jts.geom.Polygon");

    public final String descripcion;

    FieldGeoTypes(String descripcion) {
        this.descripcion = descripcion;
    }

    public static FieldGeoTypes getFieldGeoTypes(String geoTypes) {
        if (geoTypes != null && !"null".equals(geoTypes)) {
            try {
                return FieldGeoTypes.valueOf(geoTypes);
            }
            catch (java.lang.IllegalArgumentException ex) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}