package com.eatease.eatease.service;

import com.eatease.eatease.dto.IngredienteQuantDTO;
import com.eatease.eatease.dto.PedidoFastGetDTO;
import com.eatease.eatease.model.Funcionario;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.model.Mesa;
import com.eatease.eatease.model.Pedido;
import com.eatease.eatease.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemService itemService;
    private final MesaService mesaService;
    private final FuncionarioService funcionarioService;
    private final EstadoPedidoService estadoPedidoService;
    private final IngredientesService ingredientesService;

    public PedidoService(PedidoRepository PedidoRepository, ItemService itemService,
            MesaService mesaService, FuncionarioService funcionarioService,
            IngredientesService ingredientesService,
            EstadoPedidoService estadoPedidoService) {
        this.pedidoRepository = PedidoRepository;
        this.itemService = itemService;
        this.mesaService = mesaService;
        this.funcionarioService = funcionarioService;
        this.ingredientesService = ingredientesService;
        this.estadoPedidoService = estadoPedidoService;
    }

    public String checkAllInfo(List<Long> prato_id, long estadoPedido_id, long mesa_id, long funcionario_id) {
        // Verifica se o prato/estadoPedido/mesa/funcionario existe
        for (Long prato : prato_id) {
            Optional<Item> pratoOpt = itemService.getItemById(prato);
            if (pratoOpt.isEmpty()) {
                System.err.println("O prato não existe.");
                return "O prato não existe.";
            }
        }
        Optional<Mesa> mesaOpt = mesaService.getMesaById(mesa_id);
        if (mesaOpt.isEmpty()) {
            System.err.println("A mesa não existe.");
            return "A mesa não existe.";
        }

        if (!estadoPedidoService.existsById(estadoPedido_id)) {
            System.err.println("O estado do pedido não existe.");
            return "O estado do pedido não existe.";
        }

        Optional<Funcionario> funcionarioOpt = funcionarioService.getFuncionarioById(funcionario_id);
        if (funcionarioOpt.isEmpty()) {
            System.err.println("O funcionário não existe.");
            return "O funcionário não existe.";
        }
        return null;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public List<Long> temIngredientesSuficientes(Item prato, List<IngredienteQuantDTO> ingredientes) {
        List<Long> naoHaStockSuficiente = new java.util.ArrayList<>();
        for (IngredienteQuantDTO ingredienteQuant : ingredientes) {
            if (!ingredientesService.temStockSuficiente(ingredienteQuant.getIngredienteId(),
                    ingredienteQuant.getQuantidade())) {
                System.err.println("Não há stock suficiente do ingrediente " + ingredienteQuant.getIngredienteId());
                naoHaStockSuficiente.add(ingredienteQuant.getIngredienteId());
            }
        }
        return naoHaStockSuficiente;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean alterarStockItem(long prato_id, List<Long> ingredientesRemover) {
        Optional<Item> pratoOpt = itemService.getItemById(prato_id);
        if (pratoOpt.isPresent()) {
            Item prato = pratoOpt.get();
            List<IngredienteQuantDTO> ingredientes = itemService.getIngredientesByItemId(prato.getId());
            if (ingredientes == null || ingredientes.isEmpty()) {
                System.err.println("O prato não tem ingredientes.");
                return false;
            }

            // remover os ingredientes retirados do prato da lista de ingredientes que vai
            // ser removida do stock
            for (Long ingredienteRemover : ingredientesRemover) {
                for (IngredienteQuantDTO ingrediente : ingredientes) {
                    if (ingrediente.getIngredienteId() == ingredienteRemover) {
                        ingredientes.remove(ingrediente);
                        break;
                    }
                }
            }

            if (temIngredientesSuficientes(prato, ingredientes).size() > 0) {
                System.err.println("Não há stock suficiente para o prato " + prato.getNome());
                return false;
            }

            // remove stock dos ingredientes
            for (IngredienteQuantDTO ingredienteQuant : ingredientes) {
                ingredientesService.removeStock(ingredienteQuant.getIngredienteId(), ingredienteQuant.getQuantidade());
            }
            return true;
        } else {
            System.err.println("O prato não existe.");
            return false;
        }
    }

    public Pedido createPedido(List<Long> prato_id, long mesa_id, long funcionario_id,
            String observacao, List<Long> itensRemover) throws Exception {

        long estadoPedido_id = 5; // ID do estado "Pendente"

        String error = checkAllInfo(prato_id, estadoPedido_id, mesa_id, funcionario_id);
        if (error != null) {
            throw new Exception(error);
        }

        for(Long prato : prato_id) {
            Item item = itemService.getByIdNoUpdate(prato);
            if (item == null) {
                throw new Exception("O prato com ID " + prato + " não existe.");
            }
            List<IngredienteQuantDTO> ingredientes = itemService.getIngredientesByItemId(item.getId());
            if (ingredientes == null || ingredientes.isEmpty()) {
                throw new Exception("O prato " + item.getNome() + " não tem ingredientes.");
            }
            List<Long> ingredientesInsuficientes = temIngredientesSuficientes(item, ingredientes);
            if (!ingredientesInsuficientes.isEmpty()) {
                throw new Exception("Não há stock suficiente para o prato " + prato);
            }
        }

        for (Long prato : prato_id) {
            if (alterarStockItem(prato, itensRemover) == false) {
                throw new Exception("Não há stock suficiente para o prato " + prato);
            }
        }

        Pedido Pedido = new Pedido();
        Pedido.setItensIds(prato_id);
        Pedido.setEstadoPedido_id(estadoPedido_id);
        Pedido.setMesa_id(mesa_id);
        Pedido.setFuncionario_id(funcionario_id);
        Pedido.setDataHora(java.time.LocalDateTime.now().toString());
        Pedido.setObservacao(observacao);
        Pedido.setIngredientesRemover(itensRemover);
        pedidoRepository.save(Pedido);
        System.err.println("Pedido adicionado com sucesso.");
        return Pedido; // sucesso
    }

    // public String updatePedido(long id, long prato_id, long estadoPedido_id, long
    // mesa_id, long funcionario_id,
    // String observacao) {

    // String error = checkAllInfo(prato_id, estadoPedido_id, mesa_id,
    // funcionario_id);
    // if (error != null) {
    // return error;
    // }

    // if (alterarStockItem(prato_id) == false) {
    // return "Não há stock suficiente para o prato " + prato_id;
    // }
    // Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
    // if (pedidoOpt.isPresent()) {
    // Pedido pedido = pedidoOpt.get();
    // pedido.setPrato_id(prato_id);
    // pedido.setEstadoPedido_id(estadoPedido_id);
    // pedido.setMesa_id(mesa_id);
    // pedido.setFuncionario_id(funcionario_id);
    // pedido.setDataHora(java.time.LocalDateTime.now().toString());
    // pedido.setObservacao(observacao);
    // pedidoRepository.save(pedido);
    // System.err.println("Pedido atualizado com sucesso.");
    // return null; // sucesso
    // } else {
    // System.err.println("O pedido não existe.");
    // return "O pedido não existe.";
    // }
    // }

    public boolean deletePedido(long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            System.err.println("Pedido removido com sucesso.");
            return true;
        } else {
            System.err.println("O pedido não existe.");
            return false;
        }
    }

    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    public static <T> CompletableFuture<List<T>> routine(IntFunction<CompletableFuture<T>> func, int vezes) {
        List<CompletableFuture<T>> futuros = IntStream.range(0, vezes)
                .mapToObj(func)
                .toList();

        return CompletableFuture.allOf(futuros.toArray(new CompletableFuture[0]))
                .thenApply(v -> futuros.stream().map(CompletableFuture::join).toList());
    }

    public List<PedidoFastGetDTO> getAllPedidosFastGet() {
        List<Pedido> allPedidos = getAllPedidos();
        if (allPedidos.isEmpty()) {
            return new ArrayList<>();
        }

        // Collect all unique IDs to batch load data
        List<Long> allItemIds = allPedidos.stream()
                .flatMap(pedido -> pedido.getItensIds().stream())
                .distinct()
                .collect(Collectors.toList());

        List<Long> allMesaIds = allPedidos.stream()
                .map(Pedido::getMesa_id)
                .distinct()
                .collect(Collectors.toList());

        List<Long> allFuncionarioIds = allPedidos.stream()
                .map(Pedido::getFuncionario_id)
                .distinct()
                .collect(Collectors.toList());

        // Batch load all required data
        Map<Long, Item> itemsMap = batchLoadItems(allItemIds);
        Map<Long, Mesa> mesasMap = batchLoadMesas(allMesaIds);
        Map<Long, Funcionario> funcionariosMap = batchLoadFuncionarios(allFuncionarioIds);

        // Transform pedidos using pre-loaded data
        return allPedidos.stream()
                .map(currPedido -> {
                    PedidoFastGetDTO pedido = new PedidoFastGetDTO();
                    pedido.setId(currPedido.getId());

                    // Use pre-loaded items
                    pedido.setItensIds(currPedido.getItensIds().stream()
                            .map(itemsMap::get)
                            .filter(item -> item != null)
                            .collect(Collectors.toList()));

                    pedido.setEstadoPedido_id(currPedido.getEstadoPedido_id());

                    // Use pre-loaded mesa
                    Mesa mesa = mesasMap.get(currPedido.getMesa_id());
                    pedido.setMesa_number(mesa != null ? mesa.getNumero() : 0);

                    // Use pre-loaded funcionario
                    Funcionario funcionario = funcionariosMap.get(currPedido.getFuncionario_id());
                    pedido.setFuncionario(funcionario != null ? funcionario.getNome() : "");

                    pedido.setDataHora(currPedido.getDataHora());
                    pedido.setObservacao(currPedido.getObservacao());
                    pedido.setIngredientesRemover(currPedido.getIngredientesRemover() != null
                            ? currPedido.getIngredientesRemover()
                            : new ArrayList<>());

                    return pedido;
                })
                .collect(Collectors.toList());
    }

    public Optional<Pedido> getPedidoById(long id) {
        return pedidoRepository.findById(id);
    }

    private void retornarIngredientesAoStock(Pedido pedido) {
        List<Long> itens = pedido.getItensIds();
        List<Long> ingredientesRemover = pedido.getIngredientesRemover();

        // Se a lista de ingredientes a remover é null, inicializa como lista vazia
        if (ingredientesRemover == null) {
            ingredientesRemover = new java.util.ArrayList<>();
        }

        for (Long itemId : itens) {
            Item item = itemService.getByIdNoUpdate(itemId);
            if (item != null) {
                List<IngredienteQuantDTO> ingredientes = itemService.getIngredientesByItemId(item.getId());
                if (ingredientes != null) {
                    for (IngredienteQuantDTO ingrediente : ingredientes) {
                        // Só retorna ao stock os ingredientes que foram efetivamente consumidos
                        // (ou seja, que NÃO estão na lista de ingredientes removidos)
                        if (!ingredientesRemover.contains(ingrediente.getIngredienteId())) {
                            ingredientesService.addStock(ingrediente.getIngredienteId(), ingrediente.getQuantidade());
                        }
                    }
                }
            }
        }
        System.err.println("Ingredientes retornados ao estoque com sucesso (excluindo ingredientes removidos).");
    }

    @Transactional
    public String setEstadoPedido(long id, long estadoPedido_id) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isEmpty()) {
            System.err.println("O pedido não existe.");
            return "O pedido não existe.";
        }

        Pedido pedido = pedidoOpt.get();
        long currentEstadoPedidoId = pedido.getEstadoPedido_id();

        if (currentEstadoPedidoId == 4) {
            System.err.println("O pedido já está cancelado.");
            return "O pedido já está cancelado.";
        }

        if (currentEstadoPedidoId == estadoPedido_id) {
            System.err.println("O pedido já está no mesmo estado.");
            return "O pedido já está no mesmo estado.";
        }

        if (estadoPedido_id == 4) { // ID do estado "Cancelado"
            retornarIngredientesAoStock(pedido);
        }

        pedido.setEstadoPedido_id(estadoPedido_id);
        pedidoRepository.save(pedido);
        System.err.println("Estado do pedido atualizado com sucesso.");
        return null; // sucesso
    }

    private Map<Long, Item> batchLoadItems(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        // Use new batch loading method - much more efficient!
        return itemService.getItemsByIds(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, item -> item));
    }

    private Map<Long, Mesa> batchLoadMesas(List<Long> mesaIds) {
        if (mesaIds.isEmpty()) {
            return Map.of();
        }
        // Use new batch loading method
        return mesaService.getMesasByIds(mesaIds).stream()
                .collect(Collectors.toMap(Mesa::getId, mesa -> mesa));
    }

    private Map<Long, Funcionario> batchLoadFuncionarios(List<Long> funcionarioIds) {
        if (funcionarioIds.isEmpty()) {
            return Map.of();
        }
        // Use new batch loading method
        return funcionarioService.getFuncionariosByIds(funcionarioIds).stream()
                .collect(Collectors.toMap(Funcionario::getId, funcionario -> funcionario));
    }
}
