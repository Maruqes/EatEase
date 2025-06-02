package com.eatease.eatease.service;

import com.eatease.eatease.dto.IngredienteQuantDTO;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.repository.ItemRepository;
import com.eatease.eatease.repository.MenuRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IngredientesService ingredientesService;

    @Mock
    private TipoPratoService tipoPratoService;

    @Mock
    private MenuRepository menuRepository;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository, objectMapper, ingredientesService, tipoPratoService,
                menuRepository);
    }

    // ========================== CREATE ITEM TESTS ==========================

    @Test
    void testCreateItem_Success() throws Exception {
        // Arrange
        String nome = "Pizza Margherita";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;

        IngredienteQuantDTO ingrediente1 = new IngredienteQuantDTO();
        ingrediente1.setIngredienteId(1L);
        ingrediente1.setQuantidade(2);

        IngredienteQuantDTO ingrediente2 = new IngredienteQuantDTO();
        ingrediente2.setIngredienteId(2L);
        ingrediente2.setQuantidade(3);

        List<IngredienteQuantDTO> ingredientes = Arrays.asList(ingrediente1, ingrediente2);
        String ingredientesJson = "[{\"ingredienteId\":1,\"quantidade\":2},{\"ingredienteId\":2,\"quantidade\":3}]";

        when(itemRepository.findByNome(nome)).thenReturn(Optional.empty());
        when(ingredientesService.doesIngredienteExist(1L)).thenReturn(true);
        when(ingredientesService.doesIngredienteExist(2L)).thenReturn(true);
        when(tipoPratoService.checkTipoPratoExists(tipoPratoId)).thenReturn(true);
        when(objectMapper.writeValueAsString(ingredientes)).thenReturn(ingredientesJson);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        // Act
        Item result = itemService.createItem(nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual);

        // Assert
        assertNotNull(result);
        assertEquals(nome, result.getNome());
        assertEquals(tipoPratoId, result.getTipoPrato_id());
        assertEquals(preco, result.getPreco());
        assertEquals(eComposto, result.iseComposto());
        assertEquals(stockAtual, result.getStockAtual());
        assertNull(result.getFoto());

        verify(itemRepository).findByNome(nome);
        verify(ingredientesService).doesIngredienteExist(1L);
        verify(ingredientesService).doesIngredienteExist(2L);
        verify(tipoPratoService).checkTipoPratoExists(tipoPratoId);
        verify(objectMapper).writeValueAsString(ingredientes);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void testCreateItem_NullIngredientes() {
        // Arrange
        String nome = "Pizza Margherita";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.createItem(nome, tipoPratoId, preco, null, eComposto, stockAtual));

        assertEquals("A lista de ingredientes não pode ser nula ou vazia.", exception.getMessage());

        verify(itemRepository, never()).findByNome(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testCreateItem_EmptyIngredientes() {
        // Arrange
        String nome = "Pizza Margherita";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;
        List<IngredienteQuantDTO> ingredientes = Collections.emptyList();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.createItem(nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual));

        assertEquals("A lista de ingredientes não pode ser nula ou vazia.", exception.getMessage());

        verify(itemRepository, never()).findByNome(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testCreateItem_ItemAlreadyExists() {
        // Arrange
        String nome = "Pizza Margherita";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;

        IngredienteQuantDTO ingrediente = new IngredienteQuantDTO();
        ingrediente.setIngredienteId(1L);
        ingrediente.setQuantidade(2);
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(ingrediente);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setNome(nome);

        when(itemRepository.findByNome(nome)).thenReturn(Optional.of(existingItem));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.createItem(nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual));

        assertEquals("O item já existe.", exception.getMessage());

        verify(itemRepository).findByNome(nome);
        verify(ingredientesService, never()).doesIngredienteExist(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testCreateItem_IngredienteNotExists() {
        // Arrange
        String nome = "Pizza Margherita";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;

        IngredienteQuantDTO ingrediente = new IngredienteQuantDTO();
        ingrediente.setIngredienteId(999L);
        ingrediente.setQuantidade(2);
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(ingrediente);

        when(itemRepository.findByNome(nome)).thenReturn(Optional.empty());
        when(ingredientesService.doesIngredienteExist(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.createItem(nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual));

        assertEquals("O ingrediente com ID 999 não existe.", exception.getMessage());

        verify(itemRepository).findByNome(nome);
        verify(ingredientesService).doesIngredienteExist(999L);
        verify(tipoPratoService, never()).checkTipoPratoExists(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testCreateItem_TipoPratoNotExists() {
        // Arrange
        String nome = "Pizza Margherita";
        long tipoPratoId = 999L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;

        IngredienteQuantDTO ingrediente = new IngredienteQuantDTO();
        ingrediente.setIngredienteId(1L);
        ingrediente.setQuantidade(2);
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(ingrediente);

        when(itemRepository.findByNome(nome)).thenReturn(Optional.empty());
        when(ingredientesService.doesIngredienteExist(1L)).thenReturn(true);
        when(tipoPratoService.checkTipoPratoExists(tipoPratoId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.createItem(nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual));

        assertEquals("O tipo de prato com ID 999 não existe.", exception.getMessage());

        verify(itemRepository).findByNome(nome);
        verify(ingredientesService).doesIngredienteExist(1L);
        verify(tipoPratoService).checkTipoPratoExists(tipoPratoId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testCreateItem_JsonSerializationError() throws Exception {
        // Arrange
        String nome = "Pizza Margherita";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;

        IngredienteQuantDTO ingrediente = new IngredienteQuantDTO();
        ingrediente.setIngredienteId(1L);
        ingrediente.setQuantidade(2);
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(ingrediente);

        when(itemRepository.findByNome(nome)).thenReturn(Optional.empty());
        when(ingredientesService.doesIngredienteExist(1L)).thenReturn(true);
        when(tipoPratoService.checkTipoPratoExists(tipoPratoId)).thenReturn(true);
        when(objectMapper.writeValueAsString(ingredientes))
                .thenThrow(new JsonProcessingException("Serialization error") {
                });

        // Act & Assert
        Exception exception = assertThrows(Exception.class,
                () -> itemService.createItem(nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual));

        assertTrue(exception.getMessage().contains("Falha a serializar ingredientes"));

        verify(itemRepository).findByNome(nome);
        verify(ingredientesService).doesIngredienteExist(1L);
        verify(tipoPratoService).checkTipoPratoExists(tipoPratoId);
        verify(objectMapper).writeValueAsString(ingredientes);
        verify(itemRepository, never()).save(any());
    }

    // ========================== READ ITEM TESTS ==========================

    @Test
    void testGetAllItems() {
        // Arrange
        Item item1 = new Item();
        item1.setId(1L);
        item1.setNome("Pizza");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setNome("Hamburger");

        List<Item> items = Arrays.asList(item1, item2);
        when(itemRepository.findAll()).thenReturn(items);

        // Act
        List<Item> result = itemService.getAllItems();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Pizza", result.get(0).getNome());
        assertEquals("Hamburger", result.get(1).getNome());

        verify(itemRepository).findAll();
    }

    @Test
    void testGetItemById_ItemExists() {
        // Arrange
        long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setNome("Pizza");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        Optional<Item> result = itemService.getItemById(itemId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(itemId, result.get().getId());
        assertEquals("Pizza", result.get().getNome());

        verify(itemRepository).findById(itemId);
    }

    @Test
    void testGetItemById_ItemNotExists() {
        // Arrange
        long itemId = 999L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act
        Optional<Item> result = itemService.getItemById(itemId);

        // Assert
        assertFalse(result.isPresent());

        verify(itemRepository).findById(itemId);
    }

    @Test
    void testGetByPratoId_Success() {
        // Arrange
        long tipoPratoId = 1L;
        Item item1 = new Item();
        item1.setId(1L);
        item1.setTipoPrato_id(tipoPratoId);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setTipoPrato_id(tipoPratoId);

        List<Item> items = Arrays.asList(item1, item2);

        when(tipoPratoService.checkTipoPratoExists(tipoPratoId)).thenReturn(true);
        when(itemRepository.findByTipoPratoId(tipoPratoId)).thenReturn(items);

        // Act
        List<Item> result = itemService.getByPratoId(tipoPratoId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(tipoPratoId, result.get(0).getTipoPrato_id());
        assertEquals(tipoPratoId, result.get(1).getTipoPrato_id());

        verify(tipoPratoService).checkTipoPratoExists(tipoPratoId);
        verify(itemRepository).findByTipoPratoId(tipoPratoId);
    }

    @Test
    void testGetByPratoId_TipoPratoNotExists() {
        // Arrange
        long tipoPratoId = 999L;
        when(tipoPratoService.checkTipoPratoExists(tipoPratoId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.getByPratoId(tipoPratoId));

        assertEquals("O tipo de prato com ID 999 não existe.", exception.getMessage());

        verify(tipoPratoService).checkTipoPratoExists(tipoPratoId);
        verify(itemRepository, never()).findByTipoPratoId(any());
    }

    @Test
    void testDoesItemExist_True() {
        // Arrange
        long itemId = 1L;
        when(itemRepository.existsById(itemId)).thenReturn(true);

        // Act
        boolean result = itemService.doesItemExist(itemId);

        // Assert
        assertTrue(result);
        verify(itemRepository).existsById(itemId);
    }

    @Test
    void testDoesItemExist_False() {
        // Arrange
        long itemId = 999L;
        when(itemRepository.existsById(itemId)).thenReturn(false);

        // Act
        boolean result = itemService.doesItemExist(itemId);

        // Assert
        assertFalse(result);
        verify(itemRepository).existsById(itemId);
    }

    @Test
    void testGetByIdNoUpdate() {
        // Arrange
        long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setNome("Pizza");

        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        Item result = itemService.getByIdNoUpdate(itemId);

        // Assert
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Pizza", result.getNome());

        verify(itemRepository).existsById(itemId);
        verify(itemRepository).findById(itemId);
    }

    @Test
    void testGetByIdNoUpdate_ItemNotExists() {
        // Arrange
        long itemId = 999L;
        when(itemRepository.existsById(itemId)).thenReturn(false);

        // Act
        Item result = itemService.getByIdNoUpdate(itemId);

        // Assert
        assertNull(result);

        verify(itemRepository).existsById(itemId);
        verify(itemRepository, never()).findById(any());
    }

    // ========================== UPDATE ITEM TESTS ==========================

    @Test
    void testEditItem_Success() throws Exception {
        // Arrange
        long itemId = 1L;
        String nome = "Pizza Margherita Updated";
        long tipoPratoId = 1L;
        float preco = 18.99f;
        boolean eComposto = true;
        int stockAtual = 15;

        IngredienteQuantDTO ingrediente = new IngredienteQuantDTO();
        ingrediente.setIngredienteId(1L);
        ingrediente.setQuantidade(3);
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(ingrediente);

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setNome("Pizza Margherita");

        String ingredientesJson = "[{\"ingredienteId\":1,\"quantidade\":3}]";

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(ingredientesService.doesIngredienteExist(1L)).thenReturn(true);
        when(tipoPratoService.checkTipoPratoExists(tipoPratoId)).thenReturn(true);
        when(objectMapper.writeValueAsString(ingredientes)).thenReturn(ingredientesJson);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Item result = itemService.editItem(itemId, nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual);

        // Assert
        assertNotNull(result);
        assertEquals(nome, result.getNome());
        assertEquals(tipoPratoId, result.getTipoPrato_id());
        assertEquals(preco, result.getPreco());
        assertEquals(eComposto, result.iseComposto());
        assertEquals(stockAtual, result.getStockAtual());

        verify(itemRepository).findById(itemId);
        verify(ingredientesService).doesIngredienteExist(1L);
        verify(tipoPratoService).checkTipoPratoExists(tipoPratoId);
        verify(objectMapper).writeValueAsString(ingredientes);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void testEditItem_ItemNotExists() {
        // Arrange
        long itemId = 999L;
        String nome = "Pizza";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;

        IngredienteQuantDTO ingrediente = new IngredienteQuantDTO();
        ingrediente.setIngredienteId(1L);
        ingrediente.setQuantidade(2);
        List<IngredienteQuantDTO> ingredientes = Arrays.asList(ingrediente);

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.editItem(itemId, nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual));

        assertEquals("O item não existe.", exception.getMessage());

        verify(itemRepository).findById(itemId);
        verify(ingredientesService, never()).doesIngredienteExist(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testEditItem_NullIngredientes() {
        // Arrange
        long itemId = 1L;
        String nome = "Pizza";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.editItem(itemId, nome, tipoPratoId, preco, null, eComposto, stockAtual));

        assertEquals("A lista de ingredientes não pode ser nula ou vazia.", exception.getMessage());

        verify(itemRepository, never()).findById(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testEditItem_EmptyIngredientes() {
        // Arrange
        long itemId = 1L;
        String nome = "Pizza";
        long tipoPratoId = 1L;
        float preco = 15.99f;
        boolean eComposto = true;
        int stockAtual = 10;
        List<IngredienteQuantDTO> ingredientes = Collections.emptyList();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.editItem(itemId, nome, tipoPratoId, preco, ingredientes, eComposto, stockAtual));

        assertEquals("A lista de ingredientes não pode ser nula ou vazia.", exception.getMessage());

        verify(itemRepository, never()).findById(any());
        verify(itemRepository, never()).save(any());
    }

    // ========================== DELETE ITEM TESTS ==========================

    @Test
    void testDeleteItem_Success() {
        // Arrange
        long itemId = 1L;
        when(itemRepository.existsById(itemId)).thenReturn(true);

        // Act
        boolean result = itemService.deleteItem(itemId);

        // Assert
        assertTrue(result);
        verify(itemRepository).existsById(itemId);
        verify(itemRepository).deleteById(itemId);
    }

    @Test
    void testDeleteItem_ItemNotExists() {
        // Arrange
        long itemId = 999L;
        when(itemRepository.existsById(itemId)).thenReturn(false);

        // Act
        boolean result = itemService.deleteItem(itemId);

        // Assert
        assertFalse(result);
        verify(itemRepository).existsById(itemId);
        verify(itemRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteItemPhoto_Success() throws Exception {
        // Arrange
        long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setFoto("pizza_123.jpg");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean result = itemService.deleteItemPhoto(itemId);

        // Assert
        assertTrue(result);
        assertNull(item.getFoto());
        verify(itemRepository).findById(itemId);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void testDeleteItemPhoto_ItemNotExists() throws Exception {
        // Arrange
        long itemId = 999L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act
        boolean result = itemService.deleteItemPhoto(itemId);

        // Assert
        assertFalse(result);
        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testDeleteItemPhoto_NoPhoto() throws Exception {
        // Arrange
        long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setFoto(null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        boolean result = itemService.deleteItemPhoto(itemId);

        // Assert
        assertFalse(result);
        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
    }

    // ========================== BATCH OPERATIONS TESTS ==========================

    @Test
    void testGetItemsByIds_Success() {
        // Arrange
        List<Long> itemIds = Arrays.asList(1L, 2L, 3L);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setNome("Pizza");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setNome("Hamburger");

        Item item3 = new Item();
        item3.setId(3L);
        item3.setNome("Salada");

        List<Item> items = Arrays.asList(item1, item2, item3);

        when(itemRepository.findByIdIn(itemIds)).thenReturn(items);

        // Act
        List<Item> result = itemService.getItemsByIds(itemIds);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Pizza", result.get(0).getNome());
        assertEquals("Hamburger", result.get(1).getNome());
        assertEquals("Salada", result.get(2).getNome());

        verify(itemRepository).findByIdIn(itemIds);
    }
}
