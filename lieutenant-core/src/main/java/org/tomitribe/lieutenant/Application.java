package org.tomitribe.lieutenant;

public class Application {

    private String image;
    private String build;

    public Application(String image, String build) {
        this.image = image;
        this.build = build;
    }

    public String getImage() {
        return this.image;
    }

    public String getBuild() {
        return this.build;
    }

    public boolean isImageSet() {
        return this.image != null;
    }

    public boolean isBuildSet() {
        return this.build != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        return build != null ? build.equals(that.build) : that.build == null;

    }

    @Override
    public int hashCode() {
        int result = image != null ? image.hashCode() : 0;
        result = 31 * result + (build != null ? build.hashCode() : 0);
        return result;
    }
}
