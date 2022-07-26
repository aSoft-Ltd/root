package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.internal.artifacts.DefaultProjectDependencyFactory;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.catalog.DelegatingProjectDependency;
import org.gradle.api.internal.catalog.TypeSafeProjectDependencyFactory;
import javax.inject.Inject;

@NonNullApi
public class StdlibProjectDependency extends DelegatingProjectDependency {

    @Inject
    public StdlibProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":color-core"
     */
    public ColorCoreProjectDependency getColorCore() { return new ColorCoreProjectDependency(getFactory(), create(":color-core")); }

    /**
     * Creates a project dependency on the project at path ":color-css"
     */
    public ColorCssProjectDependency getColorCss() { return new ColorCssProjectDependency(getFactory(), create(":color-css")); }

    /**
     * Creates a project dependency on the project at path ":formatter"
     */
    public FormatterProjectDependency getFormatter() { return new FormatterProjectDependency(getFactory(), create(":formatter")); }

    /**
     * Creates a project dependency on the project at path ":identifier-core"
     */
    public IdentifierCoreProjectDependency getIdentifierCore() { return new IdentifierCoreProjectDependency(getFactory(), create(":identifier-core")); }

    /**
     * Creates a project dependency on the project at path ":identifier-generators"
     */
    public IdentifierGeneratorsProjectDependency getIdentifierGenerators() { return new IdentifierGeneratorsProjectDependency(getFactory(), create(":identifier-generators")); }

    /**
     * Creates a project dependency on the project at path ":kash-core"
     */
    public KashCoreProjectDependency getKashCore() { return new KashCoreProjectDependency(getFactory(), create(":kash-core")); }

    /**
     * Creates a project dependency on the project at path ":payments-requests-core"
     */
    public PaymentsRequestsCoreProjectDependency getPaymentsRequestsCore() { return new PaymentsRequestsCoreProjectDependency(getFactory(), create(":payments-requests-core")); }

    /**
     * Creates a project dependency on the project at path ":reakt-core"
     */
    public ReaktCoreProjectDependency getReaktCore() { return new ReaktCoreProjectDependency(getFactory(), create(":reakt-core")); }

    /**
     * Creates a project dependency on the project at path ":reakt-icons"
     */
    public ReaktIconsProjectDependency getReaktIcons() { return new ReaktIconsProjectDependency(getFactory(), create(":reakt-icons")); }

    /**
     * Creates a project dependency on the project at path ":reakt-web"
     */
    public ReaktWebProjectDependency getReaktWeb() { return new ReaktWebProjectDependency(getFactory(), create(":reakt-web")); }

    /**
     * Creates a project dependency on the project at path ":theme-core"
     */
    public ThemeCoreProjectDependency getThemeCore() { return new ThemeCoreProjectDependency(getFactory(), create(":theme-core")); }

    /**
     * Creates a project dependency on the project at path ":theme-css"
     */
    public ThemeCssProjectDependency getThemeCss() { return new ThemeCssProjectDependency(getFactory(), create(":theme-css")); }

    /**
     * Creates a project dependency on the project at path ":theme-react"
     */
    public ThemeReactProjectDependency getThemeReact() { return new ThemeReactProjectDependency(getFactory(), create(":theme-react")); }

}
