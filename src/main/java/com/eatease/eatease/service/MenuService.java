package com.eatease.eatease.service;

import com.eatease.eatease.model.Item;
import com.eatease.eatease.model.Menu;
import com.eatease.eatease.repository.MenuRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final ItemService itemService;
    private final TipoMenuService tipoMenuService;

    public MenuService(MenuRepository menuRepository,
            ItemService itemService,
            TipoMenuService tipoMenuService) {
        this.menuRepository = menuRepository;
        this.itemService = itemService;
        this.tipoMenuService = tipoMenuService;
    }

    /**
     * Cria um novo menu
     * 
     * @param nome       Nome do menu
     * @param descricao  Descrição do menu
     * @param itemsIds   Lista de IDs dos itens do menu
     * @param tipoMenuId ID do tipo de menu
     * @return null se bem-sucedido, mensagem de erro caso contrário
     */
    @Transactional
    public Menu createMenu(String nome, String descricao, List<Long> itemsIds, Long tipoMenuId) throws Exception {
        // Validate input parameters
        if (!StringUtils.hasText(nome)) {
            throw new IllegalArgumentException("O nome do menu não pode estar vazio.");
        }

        if (itemsIds == null) {
            throw new IllegalArgumentException("A lista de itens não pode ser nula.");
        }

        if (itemsIds.isEmpty()) {
            throw new IllegalArgumentException("O menu deve conter pelo menos um item.");
        }

        if (menuRepository.existsByNome(nome)) {
            throw new IllegalArgumentException("O menu com o nome '" + nome + "' já existe.");
        }

        // Validate item IDs existence
        for (Long itemId : itemsIds) {
            if (itemId == null) {
                throw new IllegalArgumentException("ID de item nulo encontrado na lista.");
            }

            if (!itemService.doesItemExist(itemId)) {
                throw new IllegalArgumentException("O item com ID " + itemId + " não existe.");
            }
        }

        // Validate tipo menu ID
        if (tipoMenuId == null) {
            throw new IllegalArgumentException("O ID do tipo de menu não pode ser nulo.");
        }

        if (!tipoMenuService.checkTipoMenuExists(tipoMenuId)) {
            throw new IllegalArgumentException("O tipo de menu com ID " + tipoMenuId + " não existe.");
        }

        Menu menu = new Menu();
        menu.setNome(nome);
        menu.setDescricao(descricao);

        // Convert List<Long> to long[] for the Menu entity
        long[] itemsArray = itemsIds.stream().mapToLong(Long::longValue).toArray();
        menu.setItems_id(itemsArray);
        menu.setTipoMenu(tipoMenuId);
        Menu savedMenu = menuRepository.save(menu);
        return savedMenu;
    }

    /**
     * Obtém todos os menus
     * 
     * @return Lista de todos os menus
     */
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    /**
     * Verifica se um menu existe pelo seu ID
     * 
     * @param id ID do menu
     * @return true se o menu existe, false caso contrário
     */
    public boolean checkMenuExists(Long id) {
        if (id == null) {
            return false;
        }
        return menuRepository.existsById(id);
    }

    /**
     * Obtém um menu pelo seu ID
     * 
     * @param id ID do menu
     * @return Menu encontrado ou empty se não existir
     */
    public Optional<Menu> getMenuById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return menuRepository.findById(id);
    }

    /**
     * Exclui um menu pelo seu ID
     * 
     * @param id ID do menu a ser excluído
     * @return true se o menu foi excluído, false caso contrário
     */
    @Transactional
    public boolean deleteMenu(Long id) {
        if (id == null) {
            return false;
        }

        if (menuRepository.existsById(id)) {
            menuRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Atualiza um menu existente
     * 
     * @param id         ID do menu a ser atualizado
     * @param nome       Novo nome do menu
     * @param descricao  Nova descrição do menu
     * @param itemsIds   Nova lista de IDs dos itens do menu
     * @param tipoMenuId Novo ID do tipo de menu
     * @return null se bem-sucedido, mensagem de erro caso contrário
     */
    @Transactional
    public Menu updateMenu(Long id, String nome, String descricao, List<Long> itemsIds, Long tipoMenuId)
            throws Exception {
        // Validate input parameters
        if (id == null) {
            throw new IllegalArgumentException("O ID do menu não pode ser nulo.");
        }

        if (!StringUtils.hasText(nome)) {
            throw new IllegalArgumentException("O nome do menu não pode estar vazio.");
        }

        Optional<Menu> menuOptional = menuRepository.findById(id);
        if (menuOptional.isEmpty()) {
            throw new IllegalArgumentException("Menu com ID " + id + " não encontrado.");
        }

        Menu menu = menuOptional.get();

        // Check if the new name conflicts with existing menus (excluding the current
        // menu)
        if (!nome.equals(menu.getNome()) && menuRepository.existsByNome(nome)) {
            throw new IllegalArgumentException("Já existe outro menu com o nome '" + nome + "'.");
        }

        if (itemsIds == null) {
            throw new IllegalArgumentException("A lista de itens não pode ser nula.");
        }

        if (itemsIds.isEmpty()) {
            throw new IllegalArgumentException("O menu deve conter pelo menos um item.");
        }

        // Validate item IDs existence
        for (Long itemId : itemsIds) {
            if (itemId == null) {
                throw new IllegalArgumentException("ID de item nulo encontrado na lista.");
            }

            if (!itemService.doesItemExist(itemId)) {
                throw new IllegalArgumentException("O item com ID " + itemId + " não existe.");
            }
        }

        // Validate tipo menu ID
        if (tipoMenuId == null) {
            throw new IllegalArgumentException("O ID do tipo de menu não pode ser nulo.");
        }

        if (!tipoMenuService.checkTipoMenuExists(tipoMenuId)) {
            throw new IllegalArgumentException("O tipo de menu com ID " + tipoMenuId + " não existe.");
        }

        menu.setNome(nome);
        menu.setDescricao(descricao);

        // Convert List<Long> to long[] for the Menu entity
        long[] itemsArray = itemsIds.stream().mapToLong(Long::longValue).toArray();
        menu.setItems_id(itemsArray);
        menu.setTipoMenu(tipoMenuId);

        Menu savedMenu = menuRepository.save(menu);
        return savedMenu;
    }

    public List<Item> getMenuItens(Long id) throws Exception {
        Optional<Menu> menuOptional = menuRepository.findById(id);
        if (menuOptional.isPresent()) {
            Menu menu = menuOptional.get();
            long[] itemsIds = menu.getItems_id();
            return itemService.getItemsByIds(itemsIds);
        } else {
            throw new Exception("Menu com ID " + id + " não encontrado.");
        }
    }
}
