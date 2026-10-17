package com.giansantos.helpdesk;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Pilar Arquitetura Monolitica Modular: verifica automaticamente que nenhum modulo acessa
 * pacotes {@code internal} de outro modulo e que nao ha ciclos entre modulos. Falha o build
 * (mvn verify) se a estrutura for violada.
 */
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(HelpdeskApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
