                         /*
 * Copyright (C) 2024 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.wicket.theme;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.Theme;
import de.agilecoders.wicket.core.util.Dependencies;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome6CssReference;
import java.util.List;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 *
 * Inspired by:
 * https://github.com/martin-g/wicket-bootstrap/blob/wicket-10.x-bootstrap-5.x/bootstrap-themes/src/main/java/de/agilecoders/wicket/themes/markup/html/material_design/MaterialDesignTheme.java
 * https://github.com/martin-g/wicket-webjars
 * 
 * @author wilelb
 */
public class ClarinBootstrap5Theme extends Theme {

    public ClarinBootstrap5Theme() {
        super("clarin-bootstrap-theme");
    }
    
    public ClarinBootstrap5Theme(String name) {
        super(name);
    }

    @Override
    public List<HeaderItem> getDependencies() {
        //HeaderItem headerItem = CssHeaderItem.forReference(ClarinBootstrap5ThemeCssReference.instance()).setId(BOOTSTRAP_THEME_MARKUP_ID);
        //return Collections.singletonList(headerItem);
        return List.of(
            //CssHeaderItem.forReference(ClarinBootstrap5ThemeCssReference.instance()).setId(BOOTSTRAP_THEME_MARKUP_ID),
            //CssHeaderItem.forReference(ClarinBootstrap5ThemeVloCssReference.instance()).setId(BOOTSTRAP_THEME_MARKUP_ID),
            CssHeaderItem.forReference(ClarinBootstrap5ThemeVcrCssReference.instance()).setId(BOOTSTRAP_THEME_MARKUP_ID),
            CssHeaderItem.forReference(FontAwesome6CssReference.instance()).setId(BOOTSTRAP_THEME_MARKUP_ID) //TODO: figure out how to load this via wicket bootstrap extension
        );
    }
    
    public static class ClarinBootstrap5ThemeCssReference extends CssResourceReference {
        private static final long serialVersionUID = 1L;

        /**
         * @return singleton instance
         */
        public static ClarinBootstrap5ThemeCssReference instance() {
            return Holder.INSTANCE;
        }

        /**
         * Singleton instance of this reference
         */
        private static final class Holder {
            private static final ClarinBootstrap5ThemeCssReference INSTANCE = new ClarinBootstrap5ThemeCssReference();
        }

        /**
         * Private constructor to prevent instantiation.
         */
        private ClarinBootstrap5ThemeCssReference() {
            super(ClarinBootstrap5ThemeCssReference.class, "css/bootstrap-theme.css");
        }

        @Override
        public List<HeaderItem> getDependencies() {
            return Dependencies.combine(super.getDependencies(),
                    CssHeaderItem.forReference(Bootstrap.getSettings().getCssResourceReference()));
        }        
    }
    
    public static class ClarinBootstrap5ThemeVloCssReference extends CssResourceReference {
        private static final long serialVersionUID = 1L;

        /**
         * @return singleton instance
         */
        public static ClarinBootstrap5ThemeVloCssReference instance() {
            return Holder.INSTANCE;
        }

        /**
         * Singleton instance of this reference
         */
        private static final class Holder {
            private static final ClarinBootstrap5ThemeVloCssReference INSTANCE = new ClarinBootstrap5ThemeVloCssReference();
        }

        /**
         * Private constructor to prevent instantiation.
         */
        private ClarinBootstrap5ThemeVloCssReference() {
            super(ClarinBootstrap5ThemeVloCssReference.class, "css/vlo.css");
        }

        @Override
        public List<HeaderItem> getDependencies() {
            return Dependencies.combine(super.getDependencies(),
                    CssHeaderItem.forReference(Bootstrap.getSettings().getCssResourceReference()));
        }        
    }
    
    public static class ClarinBootstrap5ThemeVcrCssReference extends CssResourceReference {
        private static final long serialVersionUID = 1L;

        /**
         * @return singleton instance
         */
        public static ClarinBootstrap5ThemeVcrCssReference instance() {
            return Holder.INSTANCE;
        }

        /**
         * Singleton instance of this reference
         */
        private static final class Holder {
            private static final ClarinBootstrap5ThemeVcrCssReference INSTANCE = new ClarinBootstrap5ThemeVcrCssReference();
        }

        /**
         * Private constructor to prevent instantiation.
         */
        private ClarinBootstrap5ThemeVcrCssReference() {
            super(ClarinBootstrap5ThemeVcrCssReference.class, "css/vcr.css");
        }

        @Override
        public List<HeaderItem> getDependencies() {
            return Dependencies.combine(super.getDependencies(),
                    CssHeaderItem.forReference(Bootstrap.getSettings().getCssResourceReference()));
        }        
    }
}
