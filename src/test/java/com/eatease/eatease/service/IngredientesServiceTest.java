package com.eatease.eatease.service;

import com.eatease.eatease.model.Ingredientes;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.repository.IngredientesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientesServiceTest {

    @Mock
    private IngredientesRepository ingredientesRepository;

    @Mock
    private UnidadeMedidaService unidadeMedidaService;

    @Mock
    private ItemService itemService;

    private IngredientesService ingredientesService;

    @BeforeEach
    void setUp() {
        ingredientesService = new IngredientesService(ingredientesRepository, unidadeMedidaService, itemService);
    }

    @Test
    void testCreateIngredientes_Success() throws Exception {
        // Arrange
        String nome = "Tomate";
        int stock = 100;
        int stock_min = 10;
        String unidade = "kg";
        long unidade_id = 1L;

        when(unidadeMedidaService.getUnidadeMedidaId(unidade)).thenReturn(unidade_id);
        when(ingredientesRepository.findByNome(nome)).thenReturn(Optional.empty());
        when(ingredientesRepository.save(any(Ingredientes.class))).thenAnswer(invocation -> {
            Ingredientes ingrediente = invocation.getArgument(0);
            ingrediente.setId(1L);
            return ingrediente;
        });

        // Act
        Ingredientes result = ingredientesService.createIngredientes(nome, stock, stock_min, unidade);

        // Assert
        assertNotNull(result);
        assertEquals(nome, result.getNome());
        assertEquals(stock, result.getStock());
        assertEquals(stock_min, result.getStock_min());
        assertEquals(unidade_id, result.getUnidade_id());

        verify(unidadeMedidaService).getUnidadeMedidaId(unidade);
        verify(ingredientesRepository).findByNome(nome);
        verify(ingredientesRepository).save(any(Ingredientes.class));
    }

    @Test
    void testCreateIngredientes_UnidadeNotExists() {
        // Arrange
        String nome = "Tomate";
        int stock = 100;
        int stock_min = 10;
        String unidade = "invalid_unit";

        when(unidadeMedidaService.getUnidadeMedidaId(unidade)).thenReturn(-1L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ingredientesService.createIngredientes(nome, stock, stock_min, unidade));

        assertEquals("A unidade de medida não existe.", exception.getMessage());

        verify(unidadeMedidaService).getUnidadeMedidaId(unidade);
        verify(ingredientesRepository, never()).findByNome(any());
        verify(ingredientesRepository, never()).save(any());
    }

    @Test
    void testCreateIngredientes_IngredienteAlreadyExists() {
        // Arrange
        String nome = "Tomate";
        int stock = 100;
        int stock_min = 10;
        String unidade = "kg";
        long unidade_id = 1L;

        Ingredientes existingIngrediente = createIngredienteWithId(1L, nome, stock, stock_min, unidade_id);

        when(unidadeMedidaService.getUnidadeMedidaId(unidade)).thenReturn(unidade_id);
        when(ingredientesRepository.findByNome(nome)).thenReturn(Optional.of(existingIngrediente));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ingredientesService.createIngredientes(nome, stock, stock_min, unidade));

        assertEquals("O ingredientes já existe.", exception.getMessage());

        verify(unidadeMedidaService).getUnidadeMedidaId(unidade);
        verify(ingredientesRepository).findByNome(nome);
        verify(ingredientesRepository, never()).save(any());
    }

    @Test
    void testGetAllIngredientes() {
        // Arrange
        List<Ingredientes> expectedIngredientes = Arrays.asList(
                createIngredienteWithId(1L, "Tomate", 100, 10, 1L),
                createIngredienteWithId(2L, "Cebola", 50, 5, 1L));

        when(ingredientesRepository.findAll()).thenReturn(expectedIngredientes);

        // Act
        List<Ingredientes> result = ingredientesService.getAllIngredientes();

        // Assert
        assertEquals(expectedIngredientes.size(), result.size());
        assertEquals(expectedIngredientes, result);
        verify(ingredientesRepository).findAll();
    }

    @Test
    void testGetIngredienteById_Success() {
        // Arrange
        long id = 1L;
        Ingredientes expectedIngrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);

        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(expectedIngrediente));

        // Act
        Ingredientes result = ingredientesService.getIngredienteById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedIngrediente, result);
        verify(ingredientesRepository).findById(id);
    }

    @Test
    void testGetIngredienteById_NotFound() {
        // Arrange
        long id = 1L;

        when(ingredientesRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Ingredientes result = ingredientesService.getIngredienteById(id);

        // Assert
        assertNull(result);
        verify(ingredientesRepository).findById(id);
    }

    @Test
    void testUpdateIngredientes_Success() throws Exception {
        // Arrange
        long id = 1L;
        String nome = "Tomate Atualizado";
        int stock = 150;
        int stock_min = 15;
        String unidade = "kg";
        long unidade_id = 1L;

        Ingredientes existingIngrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);

        when(unidadeMedidaService.getUnidadeMedidaId(unidade)).thenReturn(unidade_id);
        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(existingIngrediente));
        when(ingredientesRepository.save(any(Ingredientes.class))).thenReturn(existingIngrediente);

        // Act
        Ingredientes result = ingredientesService.updateIngredientes(id, nome, stock, stock_min, unidade);

        // Assert
        assertNotNull(result);
        assertEquals(nome, existingIngrediente.getNome());
        assertEquals(stock, existingIngrediente.getStock());
        assertEquals(stock_min, existingIngrediente.getStock_min());
        assertEquals(unidade_id, existingIngrediente.getUnidade_id());

        verify(unidadeMedidaService).getUnidadeMedidaId(unidade);
        verify(ingredientesRepository).findById(id);
        verify(ingredientesRepository).save(existingIngrediente);
    }

    @Test
    void testUpdateIngredientes_IngredienteNotFound() {
        // Arrange
        long id = 1L;
        String nome = "Tomate";
        int stock = 100;
        int stock_min = 10;
        String unidade = "kg";
        long unidade_id = 1L;

        when(unidadeMedidaService.getUnidadeMedidaId(unidade)).thenReturn(unidade_id);
        when(ingredientesRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ingredientesService.updateIngredientes(id, nome, stock, stock_min, unidade));

        assertEquals("O ingrediente não existe.", exception.getMessage());

        verify(unidadeMedidaService).getUnidadeMedidaId(unidade);
        verify(ingredientesRepository).findById(id);
        verify(ingredientesRepository, never()).save(any());
    }

    @Test
    void testUpdateIngredientes_UnidadeNotExists() {
        // Arrange
        long id = 1L;
        String nome = "Tomate";
        int stock = 100;
        int stock_min = 10;
        String unidade = "invalid_unit";

        when(unidadeMedidaService.getUnidadeMedidaId(unidade)).thenReturn(-1L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ingredientesService.updateIngredientes(id, nome, stock, stock_min, unidade));

        assertEquals("A unidade de medida não existe.", exception.getMessage());

        verify(unidadeMedidaService).getUnidadeMedidaId(unidade);
        verify(ingredientesRepository, never()).findById(any());
        verify(ingredientesRepository, never()).save(any());
    }

    @Test
    void testDeleteIngredientes_Success() {
        // Arrange
        long id = 1L;
        Ingredientes ingrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);
        List<Item> emptyItemList = new ArrayList<>();

        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(ingrediente));
        when(itemService.findItemsUsingIngredient(id)).thenReturn(emptyItemList);

        // Act
        boolean result = ingredientesService.deleteIngredientes(id);

        // Assert
        assertTrue(result);
        verify(ingredientesRepository).findById(id);
        verify(itemService).findItemsUsingIngredient(id);
        verify(ingredientesRepository).delete(ingrediente);
    }



    @Test
    void testAddStock_Success() {
        // Arrange
        long id = 1L;
        int stockToAdd = 50;
        Ingredientes ingrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);

        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(ingrediente));
        when(ingredientesRepository.save(any(Ingredientes.class))).thenReturn(ingrediente);

        // Act
        boolean result = ingredientesService.addStock(id, stockToAdd);

        // Assert
        assertTrue(result);
        assertEquals(150, ingrediente.getStock());
        verify(ingredientesRepository).findById(id);
        verify(ingredientesRepository).save(ingrediente);
    }

    @Test
    void testAddStock_IngredienteNotFound() {
        // Arrange
        long id = 1L;
        int stockToAdd = 50;

        when(ingredientesRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = ingredientesService.addStock(id, stockToAdd);

        // Assert
        assertFalse(result);
        verify(ingredientesRepository).findById(id);
        verify(ingredientesRepository, never()).save(any());
    }

    @Test
    void testRemoveStock_Success() {
        // Arrange
        long id = 1L;
        int stockToRemove = 30;
        Ingredientes ingrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);

        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(ingrediente));
        when(ingredientesRepository.save(any(Ingredientes.class))).thenReturn(ingrediente);

        // Act
        boolean result = ingredientesService.removeStock(id, stockToRemove);

        // Assert
        assertTrue(result);
        assertEquals(70, ingrediente.getStock());
        verify(ingredientesRepository).findById(id);
        verify(ingredientesRepository).save(ingrediente);
    }

    @Test
    void testRemoveStock_InsufficientStock() {
        // Arrange
        long id = 1L;
        int stockToRemove = 150;
        Ingredientes ingrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);

        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(ingrediente));

        // Act
        boolean result = ingredientesService.removeStock(id, stockToRemove);

        // Assert
        assertFalse(result);
        assertEquals(100, ingrediente.getStock()); // Stock should remain unchanged
        verify(ingredientesRepository).findById(id);
        verify(ingredientesRepository, never()).save(any());
    }

    @Test
    void testRemoveStock_IngredienteNotFound() {
        // Arrange
        long id = 1L;
        int stockToRemove = 50;

        when(ingredientesRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = ingredientesService.removeStock(id, stockToRemove);

        // Assert
        assertFalse(result);
        verify(ingredientesRepository).findById(id);
        verify(ingredientesRepository, never()).save(any());
    }

    @Test
    void testDoesIngredienteExist_True() {
        // Arrange
        long id = 1L;

        when(ingredientesRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = ingredientesService.doesIngredienteExist(id);

        // Assert
        assertTrue(result);
        verify(ingredientesRepository).existsById(id);
    }

    @Test
    void testDoesIngredienteExist_False() {
        // Arrange
        long id = 1L;

        when(ingredientesRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = ingredientesService.doesIngredienteExist(id);

        // Assert
        assertFalse(result);
        verify(ingredientesRepository).existsById(id);
    }

    @Test
    void testTemStockSuficiente_True() {
        // Arrange
        long id = 1L;
        int requiredStock = 50;
        Ingredientes ingrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);

        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(ingrediente));

        // Act
        boolean result = ingredientesService.temStockSuficiente(id, requiredStock);

        // Assert
        assertTrue(result);
        verify(ingredientesRepository).findById(id);
    }

    @Test
    void testTemStockSuficiente_False() {
        // Arrange
        long id = 1L;
        int requiredStock = 150;
        Ingredientes ingrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);

        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(ingrediente));

        // Act
        boolean result = ingredientesService.temStockSuficiente(id, requiredStock);

        // Assert
        assertFalse(result);
        verify(ingredientesRepository).findById(id);
    }

    @Test
    void testTemStockSuficiente_IngredienteNotFound() {
        // Arrange
        long id = 1L;
        int requiredStock = 50;

        when(ingredientesRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = ingredientesService.temStockSuficiente(id, requiredStock);

        // Assert
        assertFalse(result);
        verify(ingredientesRepository).findById(id);
    }

    @Test
    void testGetStockById_Success() {
        // Arrange
        long id = 1L;
        Ingredientes ingrediente = createIngredienteWithId(id, "Tomate", 100, 10, 1L);

        when(ingredientesRepository.findById(id)).thenReturn(Optional.of(ingrediente));

        // Act
        int result = ingredientesService.getStockById(id);

        // Assert
        assertEquals(100, result);
        verify(ingredientesRepository).findById(id);
    }

    @Test
    void testGetStockById_IngredienteNotFound() {
        // Arrange
        long id = 1L;

        when(ingredientesRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        int result = ingredientesService.getStockById(id);

        // Assert
        assertEquals(-1, result);
        verify(ingredientesRepository).findById(id);
    }

    @Test
    void testGetStocksByIds_Success() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        List<Ingredientes> ingredientes = Arrays.asList(
                createIngredienteWithId(1L, "Tomate", 100, 10, 1L),
                createIngredienteWithId(2L, "Cebola", 50, 5, 1L),
                createIngredienteWithId(3L, "Alho", 25, 3, 1L));

        when(ingredientesRepository.findAllById(ids)).thenReturn(ingredientes);

        // Act
        Map<Long, Integer> result = ingredientesService.getStocksByIds(ids);

        // Assert
        assertEquals(3, result.size());
        assertEquals(100, result.get(1L));
        assertEquals(50, result.get(2L));
        assertEquals(25, result.get(3L));
        verify(ingredientesRepository).findAllById(ids);
    }

    @Test
    void testGetStocksByIds_EmptyList() {
        // Arrange
        List<Long> ids = new ArrayList<>();
        List<Ingredientes> emptyResult = new ArrayList<>();

        when(ingredientesRepository.findAllById(ids)).thenReturn(emptyResult);

        // Act
        Map<Long, Integer> result = ingredientesService.getStocksByIds(ids);

        // Assert
        assertTrue(result.isEmpty());
        verify(ingredientesRepository).findAllById(ids);
    }

    @Test
    void testGetStocksByIds_PartialResults() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L, 99L); // 99L doesn't exist
        List<Ingredientes> ingredientes = Arrays.asList(
                createIngredienteWithId(1L, "Tomate", 100, 10, 1L),
                createIngredienteWithId(2L, "Cebola", 50, 5, 1L));

        when(ingredientesRepository.findAllById(ids)).thenReturn(ingredientes);

        // Act
        Map<Long, Integer> result = ingredientesService.getStocksByIds(ids);

        // Assert
        assertEquals(2, result.size());
        assertEquals(100, result.get(1L));
        assertEquals(50, result.get(2L));
        assertNull(result.get(99L));
        verify(ingredientesRepository).findAllById(ids);
    }

    // Helper method to create Ingredientes objects for testing
    private Ingredientes createIngredienteWithId(long id, String nome, int stock, int stock_min, long unidade_id) {
        Ingredientes ingrediente = new Ingredientes();
        ingrediente.setId(id);
        ingrediente.setNome(nome);
        ingrediente.setStock(stock);
        ingrediente.setStock_min(stock_min);
        ingrediente.setUnidade_id(unidade_id);
        return ingrediente;
    }
}
