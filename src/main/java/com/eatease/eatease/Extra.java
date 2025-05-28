package com.eatease.eatease;

import com.eatease.eatease.model.Funcionario;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.service.BrevoEmail;
import com.eatease.eatease.service.CargoService;
import com.eatease.eatease.service.EstadoPedidoService;
import com.eatease.eatease.service.FuncionarioService;
import com.eatease.eatease.service.IngredientesService;
import com.eatease.eatease.service.ItemService;
import com.eatease.eatease.service.QRService;
import com.eatease.eatease.service.TipoMenuService;
import com.eatease.eatease.service.TipoPratoService;
import com.eatease.eatease.service.UnidadeMedidaService;
import com.eatease.eatease.service.QRService.QRData;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Extra {

        private final UnidadeMedidaService unidadeMedidaService;
        private final CargoService cargoService;
        private final EstadoPedidoService estadoPedidoService;
        private final FuncionarioService funcionarioService;
        private final IngredientesService ingredientesService;
        private final TipoPratoService tipoPratoService;
        private final TipoMenuService tipoMenuService;
        private final ItemService itemService;
        private final QRService qrService;

        public Extra(CargoService cargoService, EstadoPedidoService estadoPedidoService,
                        FuncionarioService funcionarioService, IngredientesService ingredientesService,
                        UnidadeMedidaService unidadeMedidaService,
                        TipoPratoService tipoPratoService,
                        ItemService itemService,
                        QRService qrService,
                        TipoMenuService tipoMenuService) {
                this.tipoMenuService = tipoMenuService;
                this.tipoPratoService = tipoPratoService;
                this.cargoService = cargoService;
                this.estadoPedidoService = estadoPedidoService;
                this.funcionarioService = funcionarioService;
                this.itemService = itemService;
                this.unidadeMedidaService = unidadeMedidaService;
                this.ingredientesService = ingredientesService;
                this.qrService = qrService;
        }

        public void createThreadToSendStockEmails() {
                Thread stockEmailThread = new Thread(() -> {
                        while (true) {
                                try {
                                        List<Item> low5 = new ArrayList<>();
                                        List<Item> low10 = new ArrayList<>();

                                        List<Item> items = itemService.getAllItems();
                                        for (Item item : items) {
                                                int qnt = itemService.SetCalculatedStockByItemId(item.getId());
                                                if (qnt < 5) {
                                                        low5.add(item);
                                                } else if (qnt < 10) {
                                                        low10.add(item);
                                                }
                                        }

                                        // Create beautiful HTML content for email
                                        String htmlContent = createStockEmailHtml(low5, low10);

                                        // get all admins email
                                        List<Funcionario> funcionarios = funcionarioService.getAllFuncionarios();

                                        for (Funcionario funcionario : funcionarios) {
                                                if (funcionario.getCargoId() == 2) { // Assuming cargoId 2 is for admin
                                                        // Send email to each admin
                                                        BrevoEmail.sendEmail("support@eatease.com", "EatEase",
                                                                        "goncalokraken@gmail.com", null,
                                                                        "🚨 Alerta de Stock Baixo - EatEase",
                                                                        htmlContent);
                                                }
                                        }

                                        // Sleep for 12 hours (12 * 60 * 60 * 1000 milliseconds)
                                        Thread.sleep(12 * 60 * 60 * 1000);

                                } catch (Exception e) {
                                        System.out.println("Erro ao calcular stock: " + e.getMessage());
                                        try {
                                                // Sleep for 1 hour before retrying if there's an error
                                                Thread.sleep(60 * 60 * 1000);
                                        } catch (InterruptedException ie) {
                                                Thread.currentThread().interrupt();
                                                break;
                                        }
                                }
                        }
                });

                stockEmailThread.setDaemon(true);
                stockEmailThread.setName("StockEmailThread");
                stockEmailThread.start();
        }

        private String createStockEmailHtml(List<Item> low5, List<Item> low10) throws Exception {
                StringBuilder html = new StringBuilder();

                html.append("<!DOCTYPE html>");
                html.append("<html lang='pt'>");
                html.append("<head>");
                html.append("<meta charset='UTF-8'>");
                html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
                html.append("<title>Alerta de Stock - EatEase</title>");
                html.append("<style>");
                html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }");
                html.append(".container { max-width: 800px; margin: 20px auto; background: white; border-radius: 16px; box-shadow: 0 20px 60px rgba(0,0,0,0.1); overflow: hidden; }");
                html.append(".header { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); color: white; padding: 40px; text-align: center; }");
                html.append(".header h1 { margin: 0; font-size: 2.5em; font-weight: 700; text-shadow: 0 2px 4px rgba(0,0,0,0.3); }");
                html.append(".header .subtitle { font-size: 1.1em; margin-top: 10px; opacity: 0.9; }");
                html.append(".content { padding: 40px; }");
                html.append(".alert-section { margin-bottom: 40px; }");
                html.append(".alert-critical { border-left: 6px solid #e74c3c; background: linear-gradient(90deg, #fee, #fff); }");
                html.append(".alert-warning { border-left: 6px solid #f39c12; background: linear-gradient(90deg, #fff4e6, #fff); }");
                html.append(".alert-header { background: rgba(0,0,0,0.05); padding: 20px; margin: -20px -20px 20px -20px; display: flex; align-items: center; }");
                html.append(".alert-header h2 { margin: 0; font-size: 1.5em; flex: 1; }");
                html.append(".alert-header .count { background: white; color: #333; padding: 8px 16px; border-radius: 20px; font-weight: bold; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
                html.append(".items-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; padding: 20px; }");
                html.append(".item-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); transition: transform 0.3s ease; border: 2px solid #f0f0f0; }");
                html.append(".item-card:hover { transform: translateY(-4px); box-shadow: 0 8px 25px rgba(0,0,0,0.15); }");
                html.append(".item-name { font-weight: 700; font-size: 1.2em; color: #2c3e50; margin-bottom: 10px; }");
                html.append(".item-stock { font-size: 1.1em; padding: 8px 16px; border-radius: 8px; font-weight: 600; }");
                html.append(".stock-critical { background: #ffebee; color: #c62828; }");
                html.append(".stock-warning { background: #fff3e0; color: #ef6c00; }");
                html.append(".footer { background: #2c3e50; color: white; text-align: center; padding: 30px; }");
                html.append(".footer .logo { font-size: 1.5em; font-weight: bold; margin-bottom: 10px; }");
                html.append(".footer .info { opacity: 0.8; font-size: 0.9em; }");
                html.append(".no-items { text-align: center; padding: 40px; color: #27ae60; font-size: 1.2em; background: #d5f4e6; border-radius: 12px; margin: 20px 0; }");
                html.append("@media (max-width: 600px) { .container { margin: 10px; border-radius: 12px; } .header { padding: 30px 20px; } .header h1 { font-size: 2em; } .content { padding: 20px; } .items-grid { grid-template-columns: 1fr; } }");
                html.append("</style>");
                html.append("</head>");
                html.append("<body>");
                html.append("<div class='container'>");

                // Header
                html.append("<div class='header'>");
                html.append("<h1>🚨 Alerta de Stock</h1>");
                html.append("<div class='subtitle'>Sistema de Gestão EatEase</div>");
                html.append("</div>");

                // Content
                html.append("<div class='content'>");

                // Critical Stock Section (< 5)
                html.append("<div class='alert-section alert-critical'>");
                html.append("<div class='alert-header'>");
                html.append("<h2>🔴 Stock Crítico (< 5 unidades)</h2>");
                html.append("<div class='count'>").append(low5.size()).append(" items</div>");
                html.append("</div>");

                if (low5.isEmpty()) {
                        html.append("<div class='no-items'>✅ Nenhum item com stock crítico</div>");
                } else {
                        html.append("<div class='items-grid'>");
                        for (Item item : low5) {
                                int stock = itemService.SetCalculatedStockByItemId(item.getId());
                                html.append("<div class='item-card'>");
                                html.append("<div class='item-name'>").append(item.getNome()).append("</div>");
                                html.append("<div class='item-stock stock-critical'>Stock: ").append(stock)
                                                .append(" unidades</div>");
                                html.append("</div>");
                        }
                        html.append("</div>");
                }
                html.append("</div>");

                // Warning Stock Section (5-9)
                html.append("<div class='alert-section alert-warning'>");
                html.append("<div class='alert-header'>");
                html.append("<h2>🟡 Stock Baixo (5-9 unidades)</h2>");
                html.append("<div class='count'>").append(low10.size()).append(" items</div>");
                html.append("</div>");

                if (low10.isEmpty()) {
                        html.append("<div class='no-items'>✅ Nenhum item com stock baixo</div>");
                } else {
                        html.append("<div class='items-grid'>");
                        for (Item item : low10) {
                                int stock = itemService.SetCalculatedStockByItemId(item.getId());
                                html.append("<div class='item-card'>");
                                html.append("<div class='item-name'>").append(item.getNome()).append("</div>");
                                html.append("<div class='item-stock stock-warning'>Stock: ").append(stock)
                                                .append(" unidades</div>");
                                html.append("</div>");
                        }
                        html.append("</div>");
                }
                html.append("</div>");

                html.append("</div>");

                // Footer
                html.append("<div class='footer'>");
                html.append("<div class='logo'>🍽️ EatEase</div>");
                html.append("<div class='info'>Sistema de Gestão de Restaurante<br>Email automático gerado em ")
                                .append(java.time.LocalDateTime.now().format(
                                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                                .append("</div>");
                html.append("</div>");

                html.append("</div>");
                html.append("</body>");
                html.append("</html>");

                return html.toString();
        }

        void qrData() {
                // create a thread that runs every 30 mins
                Thread clearQRDataThread = new Thread(() -> {
                        while (true) {
                                try {
                                        qrService.removeAllExpiredQRData();
                                        // Sleep for 30 minutes
                                        Thread.sleep(QRData.EXPIRATION_TIME);
                                } catch (Exception e) {
                                        System.out.println("Erro ao limpar QR Data: " + e.getMessage());
                                }
                        }
                });
                clearQRDataThread.setDaemon(true);
                clearQRDataThread.setName("ClearQRDataThread");
                clearQRDataThread.start();
        }

        public void criar() {

                cargoService.createCargo("FUNCIONARIO"); // id 1
                cargoService.createCargo("GERENTE"); // id 2
                cargoService.createCargo("COZINHEIRO"); // id 3
                cargoService.createCargo("LIMPEZA"); // id 4

                // dar drop a table e rodar dnv genra direito
                estadoPedidoService.createEstadoPedido("IN_PREPARATION");
                estadoPedidoService.createEstadoPedido("READY");
                estadoPedidoService.createEstadoPedido("SERVED");
                estadoPedidoService.createEstadoPedido("CANCELED");
                estadoPedidoService.createEstadoPedido("PENDING");
                /*
                 * IN_PREPARATION - 1
                 * READY - 2
                 * SERVED - 3
                 * CANCELED - 4
                 * PENDING - 5
                 */

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

                createThreadToSendStockEmails();
                qrData();
                System.out.println("Testing data created successfully!");
        }
}
