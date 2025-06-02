package com.eatease.eatease.service;

import com.eatease.eatease.model.Funcionario;
import com.eatease.eatease.model.Cargo;
import com.eatease.eatease.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private CargoService cargoService;

    private FuncionarioService funcionarioService;

    @BeforeEach
    void setUp() {
        funcionarioService = new FuncionarioService(funcionarioRepository, cargoService);
    }

    @Test
    void testCreateFuncionario_Success() throws Exception {
        // Arrange
        String nome = "João Silva";
        long cargoId = 1L;
        String username = "joao.silva";
        String password = "password123";
        String email = "joao@example.com";
        String telefone = "123456789";
        String hashedPassword = "hashedPassword";

        when(cargoService.checkCargoIdExists(cargoId)).thenReturn(true);
        when(funcionarioRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> {
            Funcionario funcionario = invocation.getArgument(0);
            funcionario.setId(1L);
            return funcionario;
        });

        try (MockedStatic<Login> loginMock = mockStatic(Login.class)) {
            loginMock.when(() -> Login.hashPassword(password)).thenReturn(hashedPassword);

            // Act
            Funcionario result = funcionarioService.createFuncionario(nome, cargoId, username, password, email,
                    telefone);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(nome, result.getNome());
            assertEquals(cargoId, result.getCargoId());
            assertEquals(username, result.getUsername());
            assertEquals(email, result.getEmail());
            assertEquals(telefone, result.getTelefone());

            verify(cargoService).checkCargoIdExists(cargoId);
            verify(funcionarioRepository, times(2)).findByUsername(username);
            verify(funcionarioRepository).save(any(Funcionario.class));
            loginMock.verify(() -> Login.hashPassword(password));
        }
    }

    @Test
    void testCreateFuncionario_CargoNotExists() {
        // Arrange
        String nome = "João Silva";
        long cargoId = 999L;
        String username = "joao.silva";
        String password = "password123";
        String email = "joao@example.com";
        String telefone = "123456789";

        when(cargoService.checkCargoIdExists(cargoId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> funcionarioService.createFuncionario(nome, cargoId, username, password, email, telefone));

        assertEquals("O cargo não existe.", exception.getMessage());
        verify(cargoService).checkCargoIdExists(cargoId);
        verify(funcionarioRepository, never()).save(any(Funcionario.class));
    }

    @Test
    void testCreateFuncionario_UsernameAlreadyExists() {
        // Arrange
        String nome = "João Silva";
        long cargoId = 1L;
        String username = "joao.silva";
        String password = "password123";
        String email = "joao@example.com";
        String telefone = "123456789";

        Funcionario existingFuncionario = new Funcionario();
        existingFuncionario.setUsername(username);

        when(cargoService.checkCargoIdExists(cargoId)).thenReturn(true);
        when(funcionarioRepository.findByUsername(username)).thenReturn(Optional.of(existingFuncionario));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> funcionarioService.createFuncionario(nome, cargoId, username, password, email, telefone));

        assertEquals("O funcionário já existe.", exception.getMessage());
        verify(cargoService).checkCargoIdExists(cargoId);
        verify(funcionarioRepository).findByUsername(username);
        verify(funcionarioRepository, never()).save(any(Funcionario.class));
    }

    @Test
    void testFindByUsername_Success() {
        // Arrange
        String username = "joao.silva";
        Funcionario funcionario = new Funcionario();
        funcionario.setUsername(username);

        when(funcionarioRepository.findByUsername(username)).thenReturn(Optional.of(funcionario));

        // Act
        Optional<Funcionario> result = funcionarioService.findByUsername(username);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(funcionarioRepository).findByUsername(username);
    }

    @Test
    void testFindByUsername_NotFound() {
        // Arrange
        String username = "non.existent";

        when(funcionarioRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<Funcionario> result = funcionarioService.findByUsername(username);

        // Assert
        assertFalse(result.isPresent());
        verify(funcionarioRepository).findByUsername(username);
    }

    @Test
    void testCheckCargoByID_Success() {
        // Arrange
        long funcionarioId = 1L;
        long cargoId = 2L;
        Funcionario funcionario = new Funcionario();
        funcionario.setId(funcionarioId);
        funcionario.setCargoId(cargoId);

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));

        // Act
        boolean result = funcionarioService.checkCargoByID(funcionarioId, cargoId);

        // Assert
        assertTrue(result);
        verify(funcionarioRepository).findById(funcionarioId);
    }

    @Test
    void testCheckCargoByID_FuncionarioNotFound() {
        // Arrange
        long funcionarioId = 999L;
        long cargoId = 2L;

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        // Act
        boolean result = funcionarioService.checkCargoByID(funcionarioId, cargoId);

        // Assert
        assertFalse(result);
        verify(funcionarioRepository).findById(funcionarioId);
    }

    @Test
    void testCheckCargoByID_WrongCargo() {
        // Arrange
        long funcionarioId = 1L;
        long cargoId = 2L;
        long wrongCargoId = 3L;
        Funcionario funcionario = new Funcionario();
        funcionario.setId(funcionarioId);
        funcionario.setCargoId(wrongCargoId);

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));

        // Act
        boolean result = funcionarioService.checkCargoByID(funcionarioId, cargoId);

        // Assert
        assertFalse(result);
        verify(funcionarioRepository).findById(funcionarioId);
    }

    @Test
    void testCheckCargoByIDAndName_Success() {
        // Arrange
        long funcionarioId = 1L;
        long cargoId = 2L;
        String cargoNome = "GERENTE";

        Funcionario funcionario = new Funcionario();
        funcionario.setId(funcionarioId);
        funcionario.setCargoId(cargoId);

        Cargo cargo = new Cargo();
        cargo.setId(cargoId);
        cargo.setNome(cargoNome);

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));
        when(cargoService.findById(cargoId)).thenReturn(Optional.of(cargo));

        // Act
        boolean result = funcionarioService.checkCargoByIDAndName(funcionarioId, cargoNome);

        // Assert
        assertTrue(result);
        verify(funcionarioRepository).findById(funcionarioId);
        verify(cargoService).findById(cargoId);
    }

    @Test
    void testCheckCargoByIDAndName_FuncionarioNotFound() {
        // Arrange
        long funcionarioId = 999L;
        String cargoNome = "GERENTE";

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        // Act
        boolean result = funcionarioService.checkCargoByIDAndName(funcionarioId, cargoNome);

        // Assert
        assertFalse(result);
        verify(funcionarioRepository).findById(funcionarioId);
        verify(cargoService, never()).findById(anyLong());
    }

    @Test
    void testCheckCargoByUsername_Success() {
        // Arrange
        String username = "joao.silva";
        long cargoId = 2L;

        Funcionario funcionario = new Funcionario();
        funcionario.setUsername(username);
        funcionario.setCargoId(cargoId);

        when(funcionarioRepository.findByUsername(username)).thenReturn(Optional.of(funcionario));

        // Act
        boolean result = funcionarioService.checkCargoByUsername(username, cargoId);

        // Assert
        assertTrue(result);
        verify(funcionarioRepository).findByUsername(username);
    }

    @Test
    void testCheckCargoByUsernameAndName_Success() {
        // Arrange
        String username = "joao.silva";
        String cargoNome = "GERENTE";
        long cargoId = 2L;

        Funcionario funcionario = new Funcionario();
        funcionario.setUsername(username);
        funcionario.setCargoId(cargoId);

        Cargo cargo = new Cargo();
        cargo.setId(cargoId);
        cargo.setNome(cargoNome);

        when(funcionarioRepository.findByUsername(username)).thenReturn(Optional.of(funcionario));
        when(cargoService.findById(cargoId)).thenReturn(Optional.of(cargo));

        // Act
        boolean result = funcionarioService.checkCargoByUsernameAndName(username, cargoNome);

        // Assert
        assertTrue(result);
        verify(funcionarioRepository).findByUsername(username);
        verify(cargoService).findById(cargoId);
    }

    @Test
    void testDeleteFuncionario_Success() {
        // Arrange
        long funcionarioId = 1L;
        Funcionario funcionario = new Funcionario();
        funcionario.setId(funcionarioId);

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));

        // Act
        boolean result = funcionarioService.deleteFuncionario(funcionarioId);

        // Assert
        assertTrue(result);
        verify(funcionarioRepository).findById(funcionarioId);
        verify(funcionarioRepository).delete(funcionario);
    }

    @Test
    void testDeleteFuncionario_NotFound() {
        // Arrange
        long funcionarioId = 999L;

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        // Act
        boolean result = funcionarioService.deleteFuncionario(funcionarioId);

        // Assert
        assertFalse(result);
        verify(funcionarioRepository).findById(funcionarioId);
        verify(funcionarioRepository, never()).delete(any(Funcionario.class));
    }

    @Test
    void testGetAllFuncionarios() {
        // Arrange
        Funcionario funcionario1 = new Funcionario();
        funcionario1.setId(1L);
        funcionario1.setNome("João Silva");

        Funcionario funcionario2 = new Funcionario();
        funcionario2.setId(2L);
        funcionario2.setNome("Maria Santos");

        List<Funcionario> funcionarios = Arrays.asList(funcionario1, funcionario2);

        when(funcionarioRepository.findAll()).thenReturn(funcionarios);

        // Act
        List<Funcionario> result = funcionarioService.getAllFuncionarios();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João Silva", result.get(0).getNome());
        assertEquals("Maria Santos", result.get(1).getNome());
        verify(funcionarioRepository).findAll();
    }

    @Test
    void testUpdateFuncionario_Success() throws Exception {
        // Arrange
        long funcionarioId = 1L;
        String nome = "João Silva Atualizado";
        long cargoId = 2L;
        String username = "joao.silva";
        String password = "newPassword123";
        String email = "joao.novo@example.com";
        String telefone = "987654321";
        String hashedPassword = "newHashedPassword";

        Funcionario existingFuncionario = new Funcionario();
        existingFuncionario.setId(funcionarioId);
        existingFuncionario.setUsername(username);
        existingFuncionario.setCargoId(1L);

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(existingFuncionario));
        when(cargoService.checkCargoIdExists(cargoId)).thenReturn(true);
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<Login> loginMock = mockStatic(Login.class)) {
            loginMock.when(() -> Login.hashPassword(password)).thenReturn(hashedPassword);

            // Act
            Funcionario result = funcionarioService.updateFuncionario(funcionarioId, nome, cargoId, username, password,
                    email, telefone);

            // Assert
            assertNotNull(result);
            assertEquals(nome, result.getNome());
            assertEquals(cargoId, result.getCargoId());
            assertEquals(username, result.getUsername());
            assertEquals(hashedPassword, result.getPassword());
            assertEquals(email, result.getEmail());
            assertEquals(telefone, result.getTelefone());

            verify(funcionarioRepository).findById(funcionarioId);
            verify(cargoService).checkCargoIdExists(cargoId);
            verify(funcionarioRepository).save(existingFuncionario);
            loginMock.verify(() -> Login.hashPassword(password));
        }
    }

    @Test
    void testUpdateFuncionario_NotFound() {
        // Arrange
        long funcionarioId = 999L;
        String nome = "João Silva";
        long cargoId = 2L;
        String username = "joao.silva";
        String password = "password123";
        String email = "joao@example.com";
        String telefone = "123456789";

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> funcionarioService
                .updateFuncionario(funcionarioId, nome, cargoId, username, password, email, telefone));

        assertEquals("O funcionário não existe.", exception.getMessage());
        verify(funcionarioRepository).findById(funcionarioId);
        verify(funcionarioRepository, never()).save(any(Funcionario.class));
    }

    @Test
    void testUpdateFuncionario_CargoNotExists() {
        // Arrange
        long funcionarioId = 1L;
        String nome = "João Silva";
        long cargoId = 999L;
        String username = "joao.silva";
        String password = "password123";
        String email = "joao@example.com";
        String telefone = "123456789";

        Funcionario existingFuncionario = new Funcionario();
        existingFuncionario.setId(funcionarioId);
        existingFuncionario.setUsername(username);

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(existingFuncionario));
        when(cargoService.checkCargoIdExists(cargoId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> funcionarioService
                .updateFuncionario(funcionarioId, nome, cargoId, username, password, email, telefone));

        assertEquals("O cargo não existe.", exception.getMessage());
        verify(funcionarioRepository).findById(funcionarioId);
        verify(cargoService).checkCargoIdExists(cargoId);
        verify(funcionarioRepository, never()).save(any(Funcionario.class));
    }

    @Test
    void testUpdateFuncionario_DifferentUsername() {
        // Arrange
        long funcionarioId = 1L;
        String nome = "João Silva";
        long cargoId = 2L;
        String username = "different.username";
        String existingUsername = "joao.silva";
        String password = "password123";
        String email = "joao@example.com";
        String telefone = "123456789";

        Funcionario existingFuncionario = new Funcionario();
        existingFuncionario.setId(funcionarioId);
        existingFuncionario.setUsername(existingUsername);

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(existingFuncionario));
        when(cargoService.checkCargoIdExists(cargoId)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> funcionarioService
                .updateFuncionario(funcionarioId, nome, cargoId, username, password, email, telefone));

        assertEquals("O username deve ser sempre o mesmo.", exception.getMessage());
        verify(funcionarioRepository).findById(funcionarioId);
        verify(cargoService).checkCargoIdExists(cargoId);
        verify(funcionarioRepository, never()).save(any(Funcionario.class));
    }

    @Test
    void testGetFuncionarioById_Success() {
        // Arrange
        long funcionarioId = 1L;
        Funcionario funcionario = new Funcionario();
        funcionario.setId(funcionarioId);
        funcionario.setNome("João Silva");

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.of(funcionario));

        // Act
        Optional<Funcionario> result = funcionarioService.getFuncionarioById(funcionarioId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(funcionarioId, result.get().getId());
        assertEquals("João Silva", result.get().getNome());
        verify(funcionarioRepository).findById(funcionarioId);
    }

    @Test
    void testGetFuncionarioById_NotFound() {
        // Arrange
        long funcionarioId = 999L;

        when(funcionarioRepository.findById(funcionarioId)).thenReturn(Optional.empty());

        // Act
        Optional<Funcionario> result = funcionarioService.getFuncionarioById(funcionarioId);

        // Assert
        assertFalse(result.isPresent());
        verify(funcionarioRepository).findById(funcionarioId);
    }

    @Test
    void testGetFuncionariosByIds_Success() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        Funcionario funcionario1 = new Funcionario();
        funcionario1.setId(1L);
        funcionario1.setNome("João Silva");

        Funcionario funcionario2 = new Funcionario();
        funcionario2.setId(2L);
        funcionario2.setNome("Maria Santos");

        Funcionario funcionario3 = new Funcionario();
        funcionario3.setId(3L);
        funcionario3.setNome("Pedro Costa");

        List<Funcionario> funcionarios = Arrays.asList(funcionario1, funcionario2, funcionario3);

        when(funcionarioRepository.findByIdIn(ids)).thenReturn(funcionarios);

        // Act
        List<Funcionario> result = funcionarioService.getFuncionariosByIds(ids);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("João Silva", result.get(0).getNome());
        assertEquals("Maria Santos", result.get(1).getNome());
        assertEquals("Pedro Costa", result.get(2).getNome());
        verify(funcionarioRepository).findByIdIn(ids);
    }

    @Test
    void testGetFuncionariosByIds_EmptyList() {
        // Arrange
        List<Long> ids = new ArrayList<>();

        // Act
        List<Funcionario> result = funcionarioService.getFuncionariosByIds(ids);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(funcionarioRepository, never()).findByIdIn(anyList());
    }
}
