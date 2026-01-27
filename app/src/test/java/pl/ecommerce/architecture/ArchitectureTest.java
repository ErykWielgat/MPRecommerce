package pl.ecommerce.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "pl.ecommerce", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    // Reguła 1: Kontrolery nie mogą bezpośrednio używać Repozytoriów (powinny iść przez Serwis)
    @ArchTest
    static final ArchRule controllers_should_have_controller_in_name =
            classes().that().resideInAPackage("..controller..")
                    .should().haveSimpleNameEndingWith("Controller")
                    .orShould().haveSimpleName("GlobalControlerAdvice"); // Wyjątek dla Advice

    // Reguła 2: Serwisy powinny być w pakiecie 'service'
    @ArchTest
    static final ArchRule services_should_be_in_service_package =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage("..service..");

    // Reguła 3: Warstwa domenowa (Model) nie może zależeć od warstwy webowej (Controller)
    @ArchTest
    static final ArchRule model_should_not_depend_on_controllers =
            noClasses().that().resideInAPackage("..model..")
                    .should().dependOnClassesThat().resideInAPackage("..controller..");
    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..repository..");
}