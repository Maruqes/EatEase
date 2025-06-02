package com.eatease.eatease.service;

import com.eatease.eatease.model.Mesa;
import com.eatease.eatease.repository.MesaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MesaServiceTest {

    @Mock
    private MesaRepository mesaRepository;

    private MesaService mesaService;

    @BeforeEach
    void setUp() {
        mesaService = new MesaService(mesaRepository);
    }

    @Test
    void testCreateMesa_Success() throws Exception {
        // Arrange
        int numero = 1;
        boolean estadoLivre = true;
        int capacidade = 4;

        when(mesaRepository.findByNumero(numero)).thenReturn(Optional.empty());
        when(mesaRepository.save(any(Mesa.class))).thenAnswer(invocation -> {
            Mesa mesa = invocation.getArgument(0);
            mesa.setId(1L);
            return mesa;
        });

        // Act
        Mesa result = mesaService.createMesa(numero, estadoLivre, capacidade);

        // Assert
        assertNotNull(result);
        assertEquals(numero, result.getNumero());
        assertEquals(estadoLivre, result.isEstadoLivre());
        assertEquals(capacidade, result.getCapacidade());
        assertEquals(0.0f, result.getPos_x());
        assertEquals(0.0f, result.getPos_y());

        verify(mesaRepository).findByNumero(numero);
        verify(mesaRepository).save(any(Mesa.class));
    }

    @Test
    void testCreateMesa_MesaAlreadyExists() {
        // Arrange
        int numero = 1;
        boolean estadoLivre = true;
        int capacidade = 4;

        Mesa existingMesa = new Mesa();
        existingMesa.setId(1L);
        existingMesa.setNumero(numero);

        when(mesaRepository.findByNumero(numero)).thenReturn(Optional.of(existingMesa));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mesaService.createMesa(numero, estadoLivre, capacidade));

        assertEquals("A mesa já existe.", exception.getMessage());

        verify(mesaRepository).findByNumero(numero);
        verify(mesaRepository, never()).save(any(Mesa.class));
    }

    @Test
    void testSetMesaPos_Success() {
        // Arrange
        long id = 1L;
        float pos_x = 10.5f;
        float pos_y = 20.3f;

        Mesa mesa = new Mesa();
        mesa.setId(id);
        mesa.setNumero(1);
        mesa.setPos_x(0.0f);
        mesa.setPos_y(0.0f);

        when(mesaRepository.findById(id)).thenReturn(Optional.of(mesa));
        when(mesaRepository.save(any(Mesa.class))).thenReturn(mesa);

        // Act
        mesaService.SetMesaPos(id, pos_x, pos_y);

        // Assert
        assertEquals(pos_x, mesa.getPos_x());
        assertEquals(pos_y, mesa.getPos_y());

        verify(mesaRepository).findById(id);
        verify(mesaRepository).save(mesa);
    }

    @Test
    void testSetMesaPos_MesaNotFound() {
        // Arrange
        long id = 1L;
        float pos_x = 10.5f;
        float pos_y = 20.3f;

        when(mesaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        mesaService.SetMesaPos(id, pos_x, pos_y);

        // Assert
        verify(mesaRepository).findById(id);
        verify(mesaRepository, never()).save(any(Mesa.class));
    }

    @Test
    void testGetX_Success() {
        // Arrange
        long id = 1L;
        float expectedX = 15.5f;

        Mesa mesa = new Mesa();
        mesa.setId(id);
        mesa.setPos_x(expectedX);

        when(mesaRepository.findById(id)).thenReturn(Optional.of(mesa));

        // Act
        float result = mesaService.getX(id);

        // Assert
        assertEquals(expectedX, result);
        verify(mesaRepository).findById(id);
    }

    @Test
    void testGetX_MesaNotFound() {
        // Arrange
        long id = 1L;

        when(mesaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mesaService.getX(id));

        assertEquals("Mesa não encontrada com ID: " + id, exception.getMessage());
        verify(mesaRepository).findById(id);
    }

    @Test
    void testGetY_Success() {
        // Arrange
        long id = 1L;
        float expectedY = 25.3f;

        Mesa mesa = new Mesa();
        mesa.setId(id);
        mesa.setPos_y(expectedY);

        when(mesaRepository.findById(id)).thenReturn(Optional.of(mesa));

        // Act
        float result = mesaService.getY(id);

        // Assert
        assertEquals(expectedY, result);
        verify(mesaRepository).findById(id);
    }

    @Test
    void testGetY_MesaNotFound() {
        // Arrange
        long id = 1L;

        when(mesaRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mesaService.getY(id));

        assertEquals("Mesa não encontrada com ID: " + id, exception.getMessage());
        verify(mesaRepository).findById(id);
    }

    @Test
    void testGetAllMesas() {
        // Arrange
        List<Mesa> expectedMesas = Arrays.asList(
                createMesaWithId(1L, 1, true, 4),
                createMesaWithId(2L, 2, false, 6));

        when(mesaRepository.findAll()).thenReturn(expectedMesas);

        // Act
        List<Mesa> result = mesaService.getAllMesas();

        // Assert
        assertEquals(expectedMesas.size(), result.size());
        assertEquals(expectedMesas, result);
        verify(mesaRepository).findAll();
    }

    @Test
    void testGetMesaById_Success() {
        // Arrange
        long id = 1L;
        Mesa expectedMesa = createMesaWithId(id, 1, true, 4);

        when(mesaRepository.findById(id)).thenReturn(Optional.of(expectedMesa));

        // Act
        Optional<Mesa> result = mesaService.getMesaById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedMesa, result.get());
        verify(mesaRepository).findById(id);
    }

    @Test
    void testGetMesaById_NotFound() {
        // Arrange
        long id = 1L;

        when(mesaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Mesa> result = mesaService.getMesaById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(mesaRepository).findById(id);
    }

    @Test
    void testGetMesaByNumero_Success() {
        // Arrange
        int numero = 1;
        Mesa expectedMesa = createMesaWithId(1L, numero, true, 4);

        when(mesaRepository.findByNumero(numero)).thenReturn(Optional.of(expectedMesa));

        // Act
        Optional<Mesa> result = mesaService.getMesaByNumero(numero);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedMesa, result.get());
        verify(mesaRepository).findByNumero(numero);
    }

    @Test
    void testGetMesaByNumero_NotFound() {
        // Arrange
        int numero = 1;

        when(mesaRepository.findByNumero(numero)).thenReturn(Optional.empty());

        // Act
        Optional<Mesa> result = mesaService.getMesaByNumero(numero);

        // Assert
        assertFalse(result.isPresent());
        verify(mesaRepository).findByNumero(numero);
    }

    @Test
    void testDeleteMesa_Success() {
        // Arrange
        long id = 1L;

        when(mesaRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = mesaService.deleteMesa(id);

        // Assert
        assertTrue(result);
        verify(mesaRepository).existsById(id);
        verify(mesaRepository).deleteById(id);
    }

    @Test
    void testDeleteMesa_MesaNotExists() {
        // Arrange
        long id = 1L;

        when(mesaRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = mesaService.deleteMesa(id);

        // Assert
        assertFalse(result);
        verify(mesaRepository).existsById(id);
        verify(mesaRepository, never()).deleteById(id);
    }

    @Test
    void testSetMesaOcupada_Success() {
        // Arrange
        long id = 1L;
        Mesa mesa = createMesaWithId(id, 1, true, 4);

        when(mesaRepository.findById(id)).thenReturn(Optional.of(mesa));
        when(mesaRepository.save(any(Mesa.class))).thenReturn(mesa);

        // Act
        boolean result = mesaService.setMesaOcupada(id);

        // Assert
        assertTrue(result);
        assertFalse(mesa.isEstadoLivre());
        verify(mesaRepository).findById(id);
        verify(mesaRepository).save(mesa);
    }

    @Test
    void testSetMesaOcupada_MesaNotFound() {
        // Arrange
        long id = 1L;

        when(mesaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = mesaService.setMesaOcupada(id);

        // Assert
        assertFalse(result);
        verify(mesaRepository).findById(id);
        verify(mesaRepository, never()).save(any(Mesa.class));
    }

    @Test
    void testSetMesaOcupadaByNumero_Success() {
        // Arrange
        int numero = 1;
        Mesa mesa = createMesaWithId(1L, numero, true, 4);

        when(mesaRepository.findByNumero(numero)).thenReturn(Optional.of(mesa));
        when(mesaRepository.save(any(Mesa.class))).thenReturn(mesa);

        // Act
        boolean result = mesaService.setMesaOcupadaByNumero(numero);

        // Assert
        assertTrue(result);
        assertFalse(mesa.isEstadoLivre());
        verify(mesaRepository).findByNumero(numero);
        verify(mesaRepository).save(mesa);
    }

    @Test
    void testSetMesaOcupadaByNumero_MesaNotFound() {
        // Arrange
        int numero = 1;

        when(mesaRepository.findByNumero(numero)).thenReturn(Optional.empty());

        // Act
        boolean result = mesaService.setMesaOcupadaByNumero(numero);

        // Assert
        assertFalse(result);
        verify(mesaRepository).findByNumero(numero);
        verify(mesaRepository, never()).save(any(Mesa.class));
    }

    @Test
    void testSetMesaLivre_Success() {
        // Arrange
        long id = 1L;
        Mesa mesa = createMesaWithId(id, 1, false, 4);

        when(mesaRepository.findById(id)).thenReturn(Optional.of(mesa));
        when(mesaRepository.save(any(Mesa.class))).thenReturn(mesa);

        // Act
        boolean result = mesaService.setMesaLivre(id);

        // Assert
        assertTrue(result);
        assertTrue(mesa.isEstadoLivre());
        verify(mesaRepository).findById(id);
        verify(mesaRepository).save(mesa);
    }

    @Test
    void testSetMesaLivre_MesaNotFound() {
        // Arrange
        long id = 1L;

        when(mesaRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        boolean result = mesaService.setMesaLivre(id);

        // Assert
        assertFalse(result);
        verify(mesaRepository).findById(id);
        verify(mesaRepository, never()).save(any(Mesa.class));
    }

    @Test
    void testSetMesaLivreByNumero_Success() {
        // Arrange
        int numero = 1;
        Mesa mesa = createMesaWithId(1L, numero, false, 4);

        when(mesaRepository.findByNumero(numero)).thenReturn(Optional.of(mesa));
        when(mesaRepository.save(any(Mesa.class))).thenReturn(mesa);

        // Act
        boolean result = mesaService.setMesaLivreByNumero(numero);

        // Assert
        assertTrue(result);
        assertTrue(mesa.isEstadoLivre());
        verify(mesaRepository).findByNumero(numero);
        verify(mesaRepository).save(mesa);
    }

    @Test
    void testSetMesaLivreByNumero_MesaNotFound() {
        // Arrange
        int numero = 1;

        when(mesaRepository.findByNumero(numero)).thenReturn(Optional.empty());

        // Act
        boolean result = mesaService.setMesaLivreByNumero(numero);

        // Assert
        assertFalse(result);
        verify(mesaRepository).findByNumero(numero);
        verify(mesaRepository, never()).save(any(Mesa.class));
    }

    @Test
    void testGetMesasByIds_Success() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        List<Mesa> expectedMesas = Arrays.asList(
                createMesaWithId(1L, 1, true, 4),
                createMesaWithId(2L, 2, false, 6),
                createMesaWithId(3L, 3, true, 2));

        when(mesaRepository.findByIdIn(ids)).thenReturn(expectedMesas);

        // Act
        List<Mesa> result = mesaService.getMesasByIds(ids);

        // Assert
        assertEquals(expectedMesas.size(), result.size());
        assertEquals(expectedMesas, result);
        verify(mesaRepository).findByIdIn(ids);
    }

    @Test
    void testGetMesasByIds_EmptyList() {
        // Arrange
        List<Long> ids = new ArrayList<>();

        // Act
        List<Mesa> result = mesaService.getMesasByIds(ids);

        // Assert
        assertTrue(result.isEmpty());
        verify(mesaRepository, never()).findByIdIn(any());
    }

    @Test
    void testGetMesasByIds_NoResults() {
        // Arrange
        List<Long> ids = Arrays.asList(99L, 100L);
        List<Mesa> emptyResult = new ArrayList<>();

        when(mesaRepository.findByIdIn(ids)).thenReturn(emptyResult);

        // Act
        List<Mesa> result = mesaService.getMesasByIds(ids);

        // Assert
        assertTrue(result.isEmpty());
        verify(mesaRepository).findByIdIn(ids);
    }

    // Helper method to create Mesa objects for testing
    private Mesa createMesaWithId(long id, int numero, boolean estadoLivre, int capacidade) {
        Mesa mesa = new Mesa();
        mesa.setId(id);
        mesa.setNumero(numero);
        mesa.setEstadoLivre(estadoLivre);
        mesa.setCapacidade(capacidade);
        mesa.setPos_x(0.0f);
        mesa.setPos_y(0.0f);
        return mesa;
    }
}
