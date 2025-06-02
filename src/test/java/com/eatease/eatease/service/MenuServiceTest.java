package com.eatease.eatease.service;

import com.eatease.eatease.model.Item;
import com.eatease.eatease.model.Menu;
import com.eatease.eatease.repository.MenuRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private TipoMenuService tipoMenuService;

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService(menuRepository, itemService, tipoMenuService);
    }

    // ========================== CREATE MENU TESTS ==========================

    @Test
    void testCreateMenu_Success() throws Exception {
        // Arrange
        String nome = "Menu Vegetariano";
        String descricao = "Deliciosas opções vegetarianas";
        List<Long> itemsIds = Arrays.asList(1L, 2L, 3L);
        Long tipoMenuId = 1L;

        when(menuRepository.existsByNome(nome)).thenReturn(false);
        when(itemService.doesItemExist(1L)).thenReturn(true);
        when(itemService.doesItemExist(2L)).thenReturn(true);
        when(itemService.doesItemExist(3L)).thenReturn(true);
        when(tipoMenuService.checkTipoMenuExists(tipoMenuId)).thenReturn(true);
        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> {
            Menu menu = invocation.getArgument(0);
            menu.setId(1L);
            return menu;
        });

        // Act
        Menu result = menuService.createMenu(nome, descricao, itemsIds, tipoMenuId);

        // Assert
        assertNotNull(result);
        assertEquals(nome, result.getNome());
        assertEquals(descricao, result.getDescricao());
        assertEquals(tipoMenuId, result.getTipoMenu());
        assertArrayEquals(new long[] { 1L, 2L, 3L }, result.getItems_id());

        verify(menuRepository).existsByNome(nome);
        verify(itemService).doesItemExist(1L);
        verify(itemService).doesItemExist(2L);
        verify(itemService).doesItemExist(3L);
        verify(tipoMenuService).checkTipoMenuExists(tipoMenuId);
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    void testCreateMenu_NullNome() {
        // Arrange
        String nome = null;
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O nome do menu não pode estar vazio.", exception.getMessage());

        verify(menuRepository, never()).existsByNome(any());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testCreateMenu_EmptyNome() {
        // Arrange
        String nome = "";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O nome do menu não pode estar vazio.", exception.getMessage());

        verify(menuRepository, never()).existsByNome(any());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testCreateMenu_WhitespaceNome() {
        // Arrange
        String nome = "   ";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O nome do menu não pode estar vazio.", exception.getMessage());

        verify(menuRepository, never()).existsByNome(any());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testCreateMenu_NullItemsList() {
        // Arrange
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = null;
        Long tipoMenuId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("A lista de itens não pode ser nula.", exception.getMessage());

        verify(menuRepository, never()).existsByNome(any());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testCreateMenu_EmptyItemsList() {
        // Arrange
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = Collections.emptyList();
        Long tipoMenuId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O menu deve conter pelo menos um item.", exception.getMessage());

        verify(menuRepository, never()).existsByNome(any());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testCreateMenu_MenuAlreadyExists() {
        // Arrange
        String nome = "Menu Existente";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 1L;

        when(menuRepository.existsByNome(nome)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O menu com o nome 'Menu Existente' já existe.", exception.getMessage());

        verify(menuRepository).existsByNome(nome);
        verify(itemService, never()).doesItemExist(anyLong());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testCreateMenu_ItemNotExists() {
        // Arrange
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L, 999L);
        Long tipoMenuId = 1L;

        when(menuRepository.existsByNome(nome)).thenReturn(false);
        when(itemService.doesItemExist(1L)).thenReturn(true);
        when(itemService.doesItemExist(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O item com ID 999 não existe.", exception.getMessage());

        verify(menuRepository).existsByNome(nome);
        verify(itemService).doesItemExist(1L);
        verify(itemService).doesItemExist(999L);
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testCreateMenu_NullTipoMenuId() {
        // Arrange
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = null;

        when(menuRepository.existsByNome(nome)).thenReturn(false);
        when(itemService.doesItemExist(1L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O ID do tipo de menu não pode ser nulo.", exception.getMessage());

        verify(menuRepository).existsByNome(nome);
        verify(itemService).doesItemExist(1L);
        verify(tipoMenuService, never()).checkTipoMenuExists(any());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testCreateMenu_TipoMenuNotExists() {
        // Arrange
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 999L;

        when(menuRepository.existsByNome(nome)).thenReturn(false);
        when(itemService.doesItemExist(1L)).thenReturn(true);
        when(tipoMenuService.checkTipoMenuExists(tipoMenuId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.createMenu(nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O tipo de menu com ID 999 não existe.", exception.getMessage());

        verify(menuRepository).existsByNome(nome);
        verify(itemService).doesItemExist(1L);
        verify(tipoMenuService).checkTipoMenuExists(tipoMenuId);
        verify(menuRepository, never()).save(any());
    }

    // ========================== UPDATE MENU TESTS ==========================

    @Test
    void testUpdateMenu_Success() throws Exception {
        // Arrange
        Long id = 1L;
        String nome = "Menu Atualizado";
        String descricao = "Nova descrição";
        List<Long> itemsIds = Arrays.asList(2L, 3L);
        Long tipoMenuId = 2L;

        Menu existingMenu = new Menu();
        existingMenu.setId(id);
        existingMenu.setNome("Menu Original");
        existingMenu.setDescricao("Descrição original");
        existingMenu.setItems_id(new long[] { 1L });
        existingMenu.setTipoMenu(1L);

        when(menuRepository.findById(id)).thenReturn(Optional.of(existingMenu));
        when(menuRepository.existsByNome(nome)).thenReturn(false);
        when(itemService.doesItemExist(2L)).thenReturn(true);
        when(itemService.doesItemExist(3L)).thenReturn(true);
        when(tipoMenuService.checkTipoMenuExists(tipoMenuId)).thenReturn(true);
        when(menuRepository.save(any(Menu.class))).thenReturn(existingMenu);

        // Act
        Menu result = menuService.updateMenu(id, nome, descricao, itemsIds, tipoMenuId);

        // Assert
        assertNotNull(result);
        assertEquals(nome, result.getNome());
        assertEquals(descricao, result.getDescricao());
        assertEquals(tipoMenuId, result.getTipoMenu());
        assertArrayEquals(new long[] { 2L, 3L }, result.getItems_id());

        verify(menuRepository).findById(id);
        verify(menuRepository).existsByNome(nome);
        verify(itemService).doesItemExist(2L);
        verify(itemService).doesItemExist(3L);
        verify(tipoMenuService).checkTipoMenuExists(tipoMenuId);
        verify(menuRepository).save(existingMenu);
    }

    @Test
    void testUpdateMenu_SuccessWithSameName() throws Exception {
        // Arrange
        Long id = 1L;
        String nome = "Menu Original";
        String descricao = "Nova descrição";
        List<Long> itemsIds = Arrays.asList(2L);
        Long tipoMenuId = 2L;

        Menu existingMenu = new Menu();
        existingMenu.setId(id);
        existingMenu.setNome("Menu Original");
        existingMenu.setDescricao("Descrição original");
        existingMenu.setItems_id(new long[] { 1L });
        existingMenu.setTipoMenu(1L);

        when(menuRepository.findById(id)).thenReturn(Optional.of(existingMenu));
        when(itemService.doesItemExist(2L)).thenReturn(true);
        when(tipoMenuService.checkTipoMenuExists(tipoMenuId)).thenReturn(true);
        when(menuRepository.save(any(Menu.class))).thenReturn(existingMenu);

        // Act
        Menu result = menuService.updateMenu(id, nome, descricao, itemsIds, tipoMenuId);

        // Assert
        assertNotNull(result);

        verify(menuRepository).findById(id);
        verify(menuRepository, never()).existsByNome(nome); // Should not check since name is the same
        verify(itemService).doesItemExist(2L);
        verify(tipoMenuService).checkTipoMenuExists(tipoMenuId);
        verify(menuRepository).save(existingMenu);
    }

    @Test
    void testUpdateMenu_NullId() {
        // Arrange
        Long id = null;
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.updateMenu(id, nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O ID do menu não pode ser nulo.", exception.getMessage());

        verify(menuRepository, never()).findById(any());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testUpdateMenu_EmptyNome() {
        // Arrange
        Long id = 1L;
        String nome = "";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.updateMenu(id, nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O nome do menu não pode estar vazio.", exception.getMessage());

        verify(menuRepository, never()).findById(any());
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testUpdateMenu_MenuNotFound() {
        // Arrange
        Long id = 999L;
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 1L;

        when(menuRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.updateMenu(id, nome, descricao, itemsIds, tipoMenuId));

        assertEquals("Menu com ID 999 não encontrado.", exception.getMessage());

        verify(menuRepository).findById(id);
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testUpdateMenu_NameConflict() {
        // Arrange
        Long id = 1L;
        String nome = "Outro Menu";
        String descricao = "Descrição";
        List<Long> itemsIds = Arrays.asList(1L);
        Long tipoMenuId = 1L;

        Menu existingMenu = new Menu();
        existingMenu.setId(id);
        existingMenu.setNome("Menu Original");

        when(menuRepository.findById(id)).thenReturn(Optional.of(existingMenu));
        when(menuRepository.existsByNome(nome)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.updateMenu(id, nome, descricao, itemsIds, tipoMenuId));

        assertEquals("Já existe outro menu com o nome 'Outro Menu'.", exception.getMessage());

        verify(menuRepository).findById(id);
        verify(menuRepository).existsByNome(nome);
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testUpdateMenu_NullItemsList() {
        // Arrange
        Long id = 1L;
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = null;
        Long tipoMenuId = 1L;

        Menu existingMenu = new Menu();
        existingMenu.setId(id);
        existingMenu.setNome("Menu Original");

        when(menuRepository.findById(id)).thenReturn(Optional.of(existingMenu));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.updateMenu(id, nome, descricao, itemsIds, tipoMenuId));

        assertEquals("A lista de itens não pode ser nula.", exception.getMessage());

        verify(menuRepository).findById(id);
        verify(menuRepository, never()).save(any());
    }

    @Test
    void testUpdateMenu_EmptyItemsList() {
        // Arrange
        Long id = 1L;
        String nome = "Menu Teste";
        String descricao = "Descrição";
        List<Long> itemsIds = Collections.emptyList();
        Long tipoMenuId = 1L;

        Menu existingMenu = new Menu();
        existingMenu.setId(id);
        existingMenu.setNome("Menu Original");

        when(menuRepository.findById(id)).thenReturn(Optional.of(existingMenu));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> menuService.updateMenu(id, nome, descricao, itemsIds, tipoMenuId));

        assertEquals("O menu deve conter pelo menos um item.", exception.getMessage());

        verify(menuRepository).findById(id);
        verify(menuRepository, never()).save(any());
    }

    // ========================== DELETE MENU TESTS ==========================

    @Test
    void testDeleteMenu_Success() {
        // Arrange
        Long id = 1L;

        when(menuRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = menuService.deleteMenu(id);

        // Assert
        assertTrue(result);

        verify(menuRepository).existsById(id);
        verify(menuRepository).deleteById(id);
    }

    @Test
    void testDeleteMenu_MenuNotExists() {
        // Arrange
        Long id = 999L;

        when(menuRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = menuService.deleteMenu(id);

        // Assert
        assertFalse(result);

        verify(menuRepository).existsById(id);
        verify(menuRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteMenu_NullId() {
        // Arrange
        Long id = null;

        // Act
        boolean result = menuService.deleteMenu(id);

        // Assert
        assertFalse(result);

        verify(menuRepository, never()).existsById(any());
        verify(menuRepository, never()).deleteById(any());
    }

    // ========================== GET ALL MENUS TESTS ==========================

    @Test
    void testGetAllMenus_Success() {
        // Arrange
        Menu menu1 = new Menu();
        menu1.setId(1L);
        menu1.setNome("Menu 1");

        Menu menu2 = new Menu();
        menu2.setId(2L);
        menu2.setNome("Menu 2");

        List<Menu> expectedMenus = Arrays.asList(menu1, menu2);

        when(menuRepository.findAll()).thenReturn(expectedMenus);

        // Act
        List<Menu> result = menuService.getAllMenus();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedMenus, result);

        verify(menuRepository).findAll();
    }

    @Test
    void testGetAllMenus_EmptyList() {
        // Arrange
        when(menuRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Menu> result = menuService.getAllMenus();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(menuRepository).findAll();
    }

    // ========================== CHECK MENU EXISTS TESTS ==========================

    @Test
    void testCheckMenuExists_MenuExists() {
        // Arrange
        Long id = 1L;

        when(menuRepository.existsById(id)).thenReturn(true);

        // Act
        boolean result = menuService.checkMenuExists(id);

        // Assert
        assertTrue(result);

        verify(menuRepository).existsById(id);
    }

    @Test
    void testCheckMenuExists_MenuNotExists() {
        // Arrange
        Long id = 999L;

        when(menuRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = menuService.checkMenuExists(id);

        // Assert
        assertFalse(result);

        verify(menuRepository).existsById(id);
    }

    @Test
    void testCheckMenuExists_NullId() {
        // Arrange
        Long id = null;

        // Act
        boolean result = menuService.checkMenuExists(id);

        // Assert
        assertFalse(result);

        verify(menuRepository, never()).existsById(any());
    }

    // ========================== GET MENU BY ID TESTS ==========================

    @Test
    void testGetMenuById_Success() {
        // Arrange
        Long id = 1L;
        Menu expectedMenu = new Menu();
        expectedMenu.setId(id);
        expectedMenu.setNome("Menu Teste");

        when(menuRepository.findById(id)).thenReturn(Optional.of(expectedMenu));

        // Act
        Optional<Menu> result = menuService.getMenuById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedMenu, result.get());

        verify(menuRepository).findById(id);
    }

    @Test
    void testGetMenuById_MenuNotFound() {
        // Arrange
        Long id = 999L;

        when(menuRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Menu> result = menuService.getMenuById(id);

        // Assert
        assertFalse(result.isPresent());

        verify(menuRepository).findById(id);
    }

    @Test
    void testGetMenuById_NullId() {
        // Arrange
        Long id = null;

        // Act
        Optional<Menu> result = menuService.getMenuById(id);

        // Assert
        assertFalse(result.isPresent());

        verify(menuRepository, never()).findById(any());
    }

    // ========================== GET MENU ITENS TESTS ==========================

    @Test
    void testGetMenuItens_Success() throws Exception {
        // Arrange
        Long menuId = 1L;
        long[] itemIds = { 1L, 2L, 3L };

        Menu menu = new Menu();
        menu.setId(menuId);
        menu.setItems_id(itemIds);

        Item item1 = new Item();
        item1.setId(1L);
        item1.setNome("Item 1");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setNome("Item 2");

        Item item3 = new Item();
        item3.setId(3L);
        item3.setNome("Item 3");

        List<Item> expectedItems = Arrays.asList(item1, item2, item3);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(itemService.getItemsByIds(itemIds)).thenReturn(expectedItems);

        // Act
        List<Item> result = menuService.getMenuItens(menuId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedItems, result);

        verify(menuRepository).findById(menuId);
        verify(itemService).getItemsByIds(itemIds);
    }

    @Test
    void testGetMenuItens_EmptyItemsList() throws Exception {
        // Arrange
        Long menuId = 1L;
        long[] itemIds = {};

        Menu menu = new Menu();
        menu.setId(menuId);
        menu.setItems_id(itemIds);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(itemService.getItemsByIds(itemIds)).thenReturn(Collections.emptyList());

        // Act
        List<Item> result = menuService.getMenuItens(menuId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(menuRepository).findById(menuId);
        verify(itemService).getItemsByIds(itemIds);
    }
}
