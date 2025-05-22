package com.eatease.eatease;

import com.eatease.eatease.service.CargoService;
import com.eatease.eatease.service.EstadoPedidoService;
import com.eatease.eatease.service.FuncionarioService;
import com.eatease.eatease.service.IngredientesService;
import com.eatease.eatease.service.TipoMenuService;
import com.eatease.eatease.service.TipoPratoService;
import com.eatease.eatease.service.UnidadeMedidaService;
import org.springframework.stereotype.Component;

@Component
public class Testing {

        private final UnidadeMedidaService unidadeMedidaService;
        private final CargoService cargoService;
        private final EstadoPedidoService estadoPedidoService;
        private final FuncionarioService funcionarioService;
        private final IngredientesService ingredientesService;
        private final TipoPratoService tipoPratoService;
        private final TipoMenuService tipoMenuService;

        public Testing(CargoService cargoService, EstadoPedidoService estadoPedidoService,
                        FuncionarioService funcionarioService, IngredientesService ingredientesService,
                        UnidadeMedidaService unidadeMedidaService,
                        TipoPratoService tipoPratoService,
                        TipoMenuService tipoMenuService) {
                this.tipoMenuService = tipoMenuService;
                this.tipoPratoService = tipoPratoService;
                this.cargoService = cargoService;
                this.estadoPedidoService = estadoPedidoService;
                this.funcionarioService = funcionarioService;
                this.unidadeMedidaService = unidadeMedidaService;
                this.ingredientesService = ingredientesService;
        }

        public void criar() {

                cargoService.createCargo("FUNCIONARIO"); // id 1
                cargoService.createCargo("GERENTE"); // id 2
                cargoService.createCargo("COZINHEIRO"); // id 3
                cargoService.createCargo("LIMPEZA"); // id 4

                estadoPedidoService.createEstadoPedido("PENDING"); //5
                estadoPedidoService.createEstadoPedido("READY"); //2
                estadoPedidoService.createEstadoPedido("IN_PREPARATION"); //3
                estadoPedidoService.createEstadoPedido("SERVED"); //4
 
                unidadeMedidaService.createUnidadeMedida("quilos");
                unidadeMedidaService.createUnidadeMedida("gramas");
                unidadeMedidaService.createUnidadeMedida("litros");
                unidadeMedidaService.createUnidadeMedida("mililitros");
                unidadeMedidaService.createUnidadeMedida("unidades");
                unidadeMedidaService.createUnidadeMedida("doses");
                unidadeMedidaService.createUnidadeMedida("caixas");

                try {
                        funcionarioService.createFuncionario("Administrador", Long.parseLong("2"), "admin",
                                        "admin", "admin@email.com", "213123123");
                        funcionarioService.createFuncionario("Jota", Long.parseLong("1"), "jota", "jota",
                                        "jota@email.pt", "213123123");
                        funcionarioService.createFuncionario("Rafa", Long.parseLong("1"), "rafa", "rafa",
                                        "rafa@email.fr", "213123123");
                        funcionarioService.createFuncionario("Cozinheiro", Long.parseLong("3"), "coz", "coz",
                                        "coz", "213123123");
                } catch (Exception e) {
                        System.out.println("Erro ao criar funcionario: " + e.getMessage());
                }
                try {
                        ingredientesService.createIngredientes("Arroz", 10000, 1000, "gramas");
                        ingredientesService.createIngredientes("Massa", 20000, 1000, "quilos");
                        ingredientesService.createIngredientes("Batata", 35000, 5000, "unidades");
                } catch (Exception e) {
                        System.out.println("Erro ao criar ingredientes: " + e.getMessage());
                }

                tipoPratoService.createTipoPrato("Prato Principal");
                tipoPratoService.createTipoPrato("Entradas");
                tipoPratoService.createTipoPrato("Bebida");
                tipoPratoService.createTipoPrato("Sobremesa");

                tipoMenuService.createTipoMenu("Almoço");
                tipoMenuService.createTipoMenu("Jantar");
                tipoMenuService.createTipoMenu("Pequeno Almoço");
                tipoMenuService.createTipoMenu("Bebida");
                tipoMenuService.createTipoMenu("Vinho");
                tipoMenuService.createTipoMenu("Sobremesa");
        }
}
