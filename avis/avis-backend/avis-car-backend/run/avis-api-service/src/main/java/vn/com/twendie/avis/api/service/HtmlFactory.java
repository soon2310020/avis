package vn.com.twendie.avis.api.service;

public class HtmlFactory {

    private String html;

    public String getHtml() {
        return html;
    }

    private HtmlFactory(HtmlBuilder builder) {
        this.html = builder.html;
    }

    public static class HtmlBuilder {
        private String html;

        public HtmlBuilder load(String html) {
            this.html = html;
            return this;
        }

        public HtmlBuilder setVariable(String key, String value) {
            if (value != null) {
                this.html = this.html.replace("{{"+ key +"}}", value);
            }
            return this;
        }

        public HtmlFactory build(){
            return new HtmlFactory(this);
        }
    }
}
