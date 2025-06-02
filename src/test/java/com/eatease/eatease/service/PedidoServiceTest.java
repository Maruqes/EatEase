package com.eatease.eatease.service;

import com.eatease.eatease.dto.IngredienteQuantDTO;
import com.eatease.eatease.dto.PedidoFastGetDTO;
import com.eatease.eatease.model.Funcionario;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.model.Mesa;
import com.eatease.eatease.model.Pedido;
import com.eatease.eatease.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private MesaService mesaService;

    @Mock
    private FuncionarioService funcionarioService;

    @Mock
    private EstadoPedidoService estadoPedidoService;

    @Mock
    private IngredientesService ingredientesService;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepository,
                itemService,
                mesaService,
                funcionarioService,
                ingredientesService,
                estadoPedidoService);
    }

    // ========================== CHECK ALL INFO TESTS ==========================

    @Test
    void testCheckAllInfo_Success() {
        // Arrange
        List<Long> pratoIds = Arrays.asList(1L, 2L);
        long estadoPedidoId = 5L;
        long mesaId = 1L;
        long funcionarioId = 1L;

        Item item1 = createMockItem(1L, "Prato 1");
        Item item2 = createMockItem(2L, "Prato 2");
        Mesa mesa = createMockMesa(mesaId, 10);
        Funcionario funcionario = createMockFuncionario(funcionarioId, "João");

        when(itemService.getItemById(1L)).thenReturn(Optional.of(item1));
        when(itemService.getItemById(2L)).thenReturn(Optional.of(item2));
        when(mesaService.getMesaById(mesaId)).thenReturn(Optional.of(mesa));
        when(funcionarioService.getFuncionarioById(funcionarioId)).thenReturn(Optional.of(funcionario));
        when(estadoPedidoService.existsById(estadoPedidoId)).thenReturn(true);

        // Act
        String result = pedidoService.checkAllInfo(pratoIds, estadoPedidoId, mesaId, funcionarioId);

        // Assert
        assertNull(result);
        verify(itemService).getItemById(1L);
        verify(itemService).getItemById(2L);
        verify(mesaService).getMesaById(mesaId);
        verify(funcionarioService).getFuncionarioById(funcionarioId);
        verify(estadoPedidoService).existsById(estadoPedidoId);
    }

    @Test
    void testCheckAllInfo_PratoNotExists() {
        // Arrange
        List<Long> pratoIds = Arrays.asList(1L, 999L);
        long estadoPedidoId = 5L;
        long mesaId = 1L;
        long funcionarioId = 1L;

        Item item1 = createMockItem(1L, "Prato 1");

        when(itemService.getItemById(1L)).thenReturn(Optional.of(item1));
        when(itemService.getItemById(999L)).thenReturn(Optional.empty());

        // Act
        String result = pedidoService.checkAllInfo(pratoIds, estadoPedidoId, mesaId, funcionarioId);

        // Assert
        assertEquals("O prato não existe.", result);
        verify(itemService).getItemById(1L);
        verify(itemService).getItemById(999L);
    }

    @Test
    void testCheckAllInfo_MesaNotExists() {
        // Arrange
        List<Long> pratoIds = Arrays.asList(1L);
        long estadoPedidoId = 5L;
        long mesaId = 999L;
        long funcionarioId = 1L;

        Item item1 = createMockItem(1L, "Prato 1");

        when(itemService.getItemById(1L)).thenReturn(Optional.of(item1));
        when(mesaService.getMesaById(mesaId)).thenReturn(Optional.empty());

        // Act
        String result = pedidoService.checkAllInfo(pratoIds, estadoPedidoId, mesaId, funcionarioId);

        // Assert
        assertEquals("A mesa não existe.", result);
    }

    @Test
    void testCheckAllInfo_EstadoPedidoNotExists() {
        // Arrange
        List<Long> pratoIds = Arrays.asList(1L);
        long estadoPedidoId = 999L;
        long mesaId = 1L;
        long funcionarioId = 1L;

        Item item1 = createMockItem(1L, "Prato 1");
        Mesa mesa = createMockMesa(mesaId, 10);

        when(itemService.getItemById(1L)).thenReturn(Optional.of(item1));
        when(mesaService.getMesaById(mesaId)).thenReturn(Optional.of(mesa));
        when(estadoPedidoService.existsById(estadoPedidoId)).thenReturn(false);

        // Act
        String result = pedidoService.checkAllInfo(pratoIds, estadoPedidoId, mesaId, funcionarioId);

        // Assert
        assertEquals("O estado do pedido não existe.", result);
    }

    @Test
    void testCheckAllInfo_FuncionarioNotExists() {
        // Arrange
        List<Long> pratoIds = Arrays.asList(1L);
        long estadoPedidoId = 5L;
        long mesaId = 1L;
        long funcionarioId = 999L;

        Item item1 = createMockItem(1L, "Prato 1");
        Mesa mesa = createMockMesa(mesaId, 10);

        when(itemService.getItemById(1L)).thenReturn(Optional.of(item1));
        when(mesaService.getMesaById(mesaId)).thenReturn(Optional.of(mesa));
        when(estadoPedidoService.existsById(estadoPedidoId)).thenReturn(true);
        when(funcionarioService.getFuncionarioById(funcionarioId)).thenReturn(Optional.empty());

        // Act
        String result = pedidoService.checkAllInfo(pratoIds, estadoPedidoId, mesaId, funcionarioId);

        // Assert
        assertEquals("O funcionário não existe.", result);
    }

    // ========================== TEM INGREDIENTES SUFICIENTES TESTS
    // ==========================

    @Test
    void testTemIngredientesSuficientes_Success() {
        // Arrange
        Item prato = createMockItem(1L, "Prato Teste");
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(
                createMockIngredienteQuantDTO(1L, 100),
                createMockIngredienteQuantDTO(2L, 50));

        when(ingredientesService.temStockSuficiente(1L, 100)).thenReturn(true);
        when(ingredientesService.temStockSuficiente(2L, 50)).thenReturn(true);

        // Act
        List<Long> result = pedidoService.temIngredientesSuficientes(prato, ingredientes);

        // Assert
        assertTrue(result.isEmpty());
        verify(ingredientesService).temStockSuficiente(1L, 100);
        verify(ingredientesService).temStockSuficiente(2L, 50);
    }

    @Test
    void testTemIngredientesSuficientes_InsufficientStock() {
        // Arrange
        Item prato = createMockItem(1L, "Prato Teste");
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(
                createMockIngredienteQuantDTO(1L, 100),
                createMockIngredienteQuantDTO(2L, 50),
                createMockIngredienteQuantDTO(3L, 30));

        when(ingredientesService.temStockSuficiente(1L, 100)).thenReturn(true);
        when(ingredientesService.temStockSuficiente(2L, 50)).thenReturn(false);
        when(ingredientesService.temStockSuficiente(3L, 30)).thenReturn(false);

        // Act
        List<Long> result = pedidoService.temIngredientesSuficientes(prato, ingredientes);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(2L));
        assertTrue(result.contains(3L));
        assertFalse(result.contains(1L));
    }

    @Test
    void testAlterarStockItem_PratoNotExists() {
        // Arrange
        long pratoId = 999L;
        List<Long> ingredientesRemover = new ArrayList<>();

        when(itemService.getItemById(pratoId)).thenReturn(Optional.empty());

        // Act
        boolean result = pedidoService.alterarStockItem(pratoId, ingredientesRemover);

        // Assert
        assertFalse(result);
        verify(itemService).getItemById(pratoId);
        verifyNoInteractions(ingredientesService);
    }

    @Test
    void testAlterarStockItem_NoIngredients() {
        // Arrange
        long pratoId = 1L;
        List<Long> ingredientesRemover = new ArrayList<>();
        Item prato = createMockItem(pratoId, "Prato Teste");

        when(itemService.getItemById(pratoId)).thenReturn(Optional.of(prato));
        when(itemService.getIngredientesByItemId(pratoId)).thenReturn(new ArrayList<>());

        // Act
        boolean result = pedidoService.alterarStockItem(pratoId, ingredientesRemover);

        // Assert
        assertFalse(result);
    }

    @Test
    void testAlterarStockItem_InsufficientStock() {
        // Arrange
        long pratoId = 1L;
        List<Long> ingredientesRemover = new ArrayList<>();
        Item prato = createMockItem(pratoId, "Prato Teste");
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(
                createMockIngredienteQuantDTO(1L, 100));

        when(itemService.getItemById(pratoId)).thenReturn(Optional.of(prato));
        when(itemService.getIngredientesByItemId(pratoId)).thenReturn(ingredientes);
        when(ingredientesService.temStockSuficiente(1L, 100)).thenReturn(false);

        // Act
        boolean result = pedidoService.alterarStockItem(pratoId, ingredientesRemover);

        // Assert
        assertFalse(result);
        verify(ingredientesService, never()).removeStock(anyLong(), anyInt());
    }

    // ========================== CREATE PEDIDO TESTS ==========================

    @Test
    void testCreatePedido_Success() throws Exception {
        // Arrange
        List<Long> pratoIds = Arrays.asList(1L);
        long mesaId = 1L;
        long funcionarioId = 1L;
        String observacao = "Teste observação";
        List<Long> itensRemover = new ArrayList<>();

        Item item = createMockItem(1L, "Prato Teste");
        Mesa mesa = createMockMesa(mesaId, 10);
        Funcionario funcionario = createMockFuncionario(funcionarioId, "João");
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(
                createMockIngredienteQuantDTO(1L, 100));

        // Mock checkAllInfo dependencies
        when(itemService.getItemById(1L)).thenReturn(Optional.of(item));
        when(mesaService.getMesaById(mesaId)).thenReturn(Optional.of(mesa));
        when(funcionarioService.getFuncionarioById(funcionarioId)).thenReturn(Optional.of(funcionario));
        when(estadoPedidoService.existsById(5L)).thenReturn(true);

        // Mock createPedido specific dependencies
        when(itemService.getByIdNoUpdate(1L)).thenReturn(item);
        when(itemService.getIngredientesByItemId(1L)).thenReturn(ingredientes);
        when(ingredientesService.temStockSuficiente(1L, 100)).thenReturn(true);

        Pedido savedPedido = new Pedido();
        savedPedido.setId(1L);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(savedPedido);

        // Act
        Pedido result = pedidoService.createPedido(pratoIds, mesaId, funcionarioId, observacao, itensRemover);

        // Assert
        assertNotNull(result);
        verify(pedidoRepository).save(any(Pedido.class));
        verify(ingredientesService).removeStock(1L, 100);
    }

    @Test
    void testCreatePedido_PratoNotExists() {
        // Arrange
        List<Long> pratoIds = Arrays.asList(999L);
        long mesaId = 1L;
        long funcionarioId = 1L;
        String observacao = "Teste";
        List<Long> itensRemover = new ArrayList<>();

        Mesa mesa = createMockMesa(mesaId, 10);
        Funcionario funcionario = createMockFuncionario(funcionarioId, "João");

        when(itemService.getItemById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            pedidoService.createPedido(pratoIds, mesaId, funcionarioId, observacao, itensRemover);
        });

        assertEquals("O prato não existe.", exception.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void testCreatePedido_InsufficientIngredients() {
        // Arrange
        List<Long> pratoIds = Arrays.asList(1L);
        long mesaId = 1L;
        long funcionarioId = 1L;
        String observacao = "Teste";
        List<Long> itensRemover = new ArrayList<>();

        Item item = createMockItem(1L, "Prato Teste");
        Mesa mesa = createMockMesa(mesaId, 10);
        Funcionario funcionario = createMockFuncionario(funcionarioId, "João");
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(
                createMockIngredienteQuantDTO(1L, 100));

        // Mock checkAllInfo dependencies
        when(itemService.getItemById(1L)).thenReturn(Optional.of(item));
        when(mesaService.getMesaById(mesaId)).thenReturn(Optional.of(mesa));
        when(funcionarioService.getFuncionarioById(funcionarioId)).thenReturn(Optional.of(funcionario));
        when(estadoPedidoService.existsById(5L)).thenReturn(true);

        // Mock ingredient check
        when(itemService.getByIdNoUpdate(1L)).thenReturn(item);
        when(itemService.getIngredientesByItemId(1L)).thenReturn(ingredientes);
        when(ingredientesService.temStockSuficiente(1L, 100)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            pedidoService.createPedido(pratoIds, mesaId, funcionarioId, observacao, itensRemover);
        });

        assertTrue(exception.getMessage().contains("Não há stock suficiente"));
        verify(pedidoRepository, never()).save(any());
    }

    // ========================== SET ESTADO PEDIDO TESTS ==========================

    @Test
    void testSetEstadoPedido_Success() {
        // Arrange
        long pedidoId = 1L;
        long novoEstadoId = 2L;

        Pedido pedido = createMockPedido(pedidoId, Arrays.asList(1L), 5L, 1L, 1L);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        String result = pedidoService.setEstadoPedido(pedidoId, novoEstadoId);

        // Assert
        assertNull(result);
        assertEquals(novoEstadoId, pedido.getEstadoPedido_id());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void testSetEstadoPedido_PedidoNotExists() {
        // Arrange
        long pedidoId = 999L;
        long novoEstadoId = 2L;

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // Act
        String result = pedidoService.setEstadoPedido(pedidoId, novoEstadoId);

        // Assert
        assertEquals("O pedido não existe.", result);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void testSetEstadoPedido_AlreadyCanceled() {
        // Arrange
        long pedidoId = 1L;
        long novoEstadoId = 2L;

        Pedido pedido = createMockPedido(pedidoId, Arrays.asList(1L), 4L, 1L, 1L); // Estado 4 = cancelado
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // Act
        String result = pedidoService.setEstadoPedido(pedidoId, novoEstadoId);

        // Assert
        assertEquals("O pedido já está cancelado.", result);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void testSetEstadoPedido_SameState() {
        // Arrange
        long pedidoId = 1L;
        long estadoAtual = 2L;

        Pedido pedido = createMockPedido(pedidoId, Arrays.asList(1L), estadoAtual, 1L, 1L);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // Act
        String result = pedidoService.setEstadoPedido(pedidoId, estadoAtual);

        // Assert
        assertEquals("O pedido já está no mesmo estado.", result);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void testSetEstadoPedido_CancelOrder() {
        // Arrange
        long pedidoId = 1L;
        long estadoCancelado = 4L;
        List<Long> itensIds = Arrays.asList(1L);
        List<Long> ingredientesRemover = Arrays.asList(2L);

        Pedido pedido = createMockPedido(pedidoId, itensIds, 1L, 1L, 1L);
        pedido.setIngredientesRemover(ingredientesRemover);

        Item item = createMockItem(1L, "Prato Teste");
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(
                createMockIngredienteQuantDTO(1L, 100),
                createMockIngredienteQuantDTO(2L, 50));

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(itemService.getByIdNoUpdate(1L)).thenReturn(item);
        when(itemService.getIngredientesByItemId(1L)).thenReturn(ingredientes);

        // Act
        String result = pedidoService.setEstadoPedido(pedidoId, estadoCancelado);

        // Assert
        assertNull(result);
        assertEquals(estadoCancelado, pedido.getEstadoPedido_id());
        verify(ingredientesService).addStock(1L, 100); // Ingredient 1 should be returned
        verify(ingredientesService, never()).addStock(2L, 50); // Ingredient 2 was removed, should not be returned
        verify(pedidoRepository).save(pedido);
    }

    // ========================== DELETE PEDIDO TESTS ==========================

    @Test
    void testDeletePedido_Success() {
        // Arrange
        long pedidoId = 1L;
        when(pedidoRepository.existsById(pedidoId)).thenReturn(true);

        // Act
        boolean result = pedidoService.deletePedido(pedidoId);

        // Assert
        assertTrue(result);
        verify(pedidoRepository).deleteById(pedidoId);
    }

    @Test
    void testDeletePedido_NotExists() {
        // Arrange
        long pedidoId = 999L;
        when(pedidoRepository.existsById(pedidoId)).thenReturn(false);

        // Act
        boolean result = pedidoService.deletePedido(pedidoId);

        // Assert
        assertFalse(result);
        verify(pedidoRepository, never()).deleteById(anyLong());
    }

    // ========================== GET ALL PEDIDOS TESTS ==========================

    @Test
    void testGetAllPedidos() {
        // Arrange
        List<Pedido> expectedPedidos = Arrays.asList(
                createMockPedido(1L, Arrays.asList(1L), 1L, 1L, 1L),
                createMockPedido(2L, Arrays.asList(2L), 2L, 2L, 2L));
        when(pedidoRepository.findAll()).thenReturn(expectedPedidos);

        // Act
        List<Pedido> result = pedidoService.getAllPedidos();

        // Assert
        assertEquals(expectedPedidos.size(), result.size());
        assertEquals(expectedPedidos, result);
        verify(pedidoRepository).findAll();
    }

    // ========================== GET ALL PEDIDOS FAST GET TESTS
    // ==========================

    @Test
    void testGetAllPedidosFastGet_EmptyList() {
        // Arrange
        when(pedidoRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<PedidoFastGetDTO> result = pedidoService.getAllPedidosFastGet();

        // Assert
        assertTrue(result.isEmpty());
        verify(pedidoRepository).findAll();
    }

    @Test
    void testGetAllPedidosFastGet_WithData() {
        // Arrange
        List<Pedido> pedidos = Arrays.asList(
                createMockPedido(1L, Arrays.asList(1L, 2L), 1L, 1L, 1L));

        List<Item> items = Arrays.asList(
                createMockItem(1L, "Item 1"),
                createMockItem(2L, "Item 2"));

        List<Mesa> mesas = Arrays.asList(
                createMockMesa(1L, 10));

        List<Funcionario> funcionarios = Arrays.asList(
                createMockFuncionario(1L, "João"));

        when(pedidoRepository.findAll()).thenReturn(pedidos);
        when(itemService.getItemsByIds(Arrays.asList(1L, 2L))).thenReturn(items);
        when(mesaService.getMesasByIds(Arrays.asList(1L))).thenReturn(mesas);
        when(funcionarioService.getFuncionariosByIds(Arrays.asList(1L))).thenReturn(funcionarios);

        // Act
        List<PedidoFastGetDTO> result = pedidoService.getAllPedidosFastGet();

        // Assert
        assertEquals(1, result.size());
        PedidoFastGetDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals(2, dto.getItensIds().size());
        assertEquals(10, dto.getMesa_number());
        assertEquals("João", dto.getFuncionario());
    }

    // ========================== GET PEDIDO BY ID TESTS ==========================

    @Test
    void testGetPedidoById_Success() {
        // Arrange
        long pedidoId = 1L;
        Pedido expectedPedido = createMockPedido(pedidoId, Arrays.asList(1L), 1L, 1L, 1L);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(expectedPedido));

        // Act
        Optional<Pedido> result = pedidoService.getPedidoById(pedidoId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedPedido, result.get());
        verify(pedidoRepository).findById(pedidoId);
    }

    @Test
    void testGetPedidoById_NotFound() {
        // Arrange
        long pedidoId = 999L;
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // Act
        Optional<Pedido> result = pedidoService.getPedidoById(pedidoId);

        // Assert
        assertFalse(result.isPresent());
        verify(pedidoRepository).findById(pedidoId);
    }

    // ========================== ROUTINE METHOD TESTS ==========================

    @Test
    void testRoutine_SuccessfulExecution() {
        // Arrange
        IntFunction<CompletableFuture<String>> func = i -> CompletableFuture.completedFuture("Result " + i);
        int times = 3;

        // Act
        CompletableFuture<List<String>> result = PedidoService.routine(func, times);

        // Assert
        assertNotNull(result);
        List<String> results = result.join();
        assertEquals(3, results.size());
        assertEquals("Result 0", results.get(0));
        assertEquals("Result 1", results.get(1));
        assertEquals("Result 2", results.get(2));
    }

    @Test
    void testRoutine_ZeroTimes() {
        // Arrange
        IntFunction<CompletableFuture<String>> func = i -> CompletableFuture.completedFuture("Result " + i);
        int times = 0;

        // Act
        CompletableFuture<List<String>> result = PedidoService.routine(func, times);

        // Assert
        assertNotNull(result);
        List<String> results = result.join();
        assertTrue(results.isEmpty());
    }

    // ========================== ADDITIONAL EDGE CASE TESTS
    // ==========================

    @Test
    void testCheckAllInfo_EmptyItemsList() {
        // Arrange
        List<Long> emptyItems = new ArrayList<>();
        long estadoPedidoId = 1L;
        long mesaId = 1L;
        long funcionarioId = 1L;

        // Act
        String result = pedidoService.checkAllInfo(emptyItems, estadoPedidoId, mesaId, funcionarioId);

        // Assert
        // Should handle empty list gracefully
        // The method would normally check each item, but with empty list it should
        // continue to other validations
        assertNotNull(result);
    }

    @Test
    void testSetEstadoPedido_InvalidState() {
        // Arrange
        long pedidoId = 1L;
        long invalidEstadoId = 999L;

        Pedido pedido = createMockPedido(pedidoId, Arrays.asList(1L), 1L, 1L, 1L);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // Act
        String result = pedidoService.setEstadoPedido(pedidoId, invalidEstadoId);

        // Assert
        // Should update even with "invalid" state ID since validation is not enforced
        // in the method
        assertNull(result);
        assertEquals(invalidEstadoId, pedido.getEstadoPedido_id());
        verify(pedidoRepository).save(pedido);
    }

    // ========================== HELPER METHODS ==========================

    private Item createMockItem(Long id, String nome) {
        Item item = new Item();
        item.setId(id);
        item.setNome(nome);
        item.setPreco(10.0f);
        return item;
    }

    private Mesa createMockMesa(Long id, int numero) {
        Mesa mesa = new Mesa();
        mesa.setId(id);
        mesa.setNumero(numero);
        mesa.setCapacidade(4);
        return mesa;
    }

    private Funcionario createMockFuncionario(Long id, String nome) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        funcionario.setNome(nome);
        funcionario.setEmail(nome.toLowerCase() + "@test.com");
        return funcionario;
    }

    private IngredienteQuantDTO createMockIngredienteQuantDTO(Long ingredienteId, int quantidade) {
        IngredienteQuantDTO dto = new IngredienteQuantDTO();
        dto.setIngredienteId(ingredienteId);
        dto.setQuantidade(quantidade);
        return dto;
    }

    private Pedido createMockPedido(Long id, List<Long> itensIds, Long estadoPedidoId, Long mesaId,
            Long funcionarioId) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setItensIds(itensIds);
        pedido.setEstadoPedido_id(estadoPedidoId);
        pedido.setMesa_id(mesaId);
        pedido.setFuncionario_id(funcionarioId);
        pedido.setDataHora(LocalDateTime.now().toString());
        pedido.setObservacao("Observação teste");
        pedido.setIngredientesRemover(new ArrayList<>());
        return pedido;
    }
}
