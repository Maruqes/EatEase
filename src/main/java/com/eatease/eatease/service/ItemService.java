package com.eatease.eatease.service;

import com.eatease.eatease.dto.IngredienteQuantDTO;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.model.Menu;
import com.eatease.eatease.repository.ItemRepository;
import com.eatease.eatease.repository.MenuRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper; // Jackson
    private final IngredientesService ingredientesService;
    private final TipoPratoService tipoPratoService;
    private final MenuRepository menuRepository;

    public ItemService(ItemRepository itemRepository, ObjectMapper objectMapper,
            IngredientesService ingredientesService, TipoPratoService tipoPratoService,
            MenuRepository menuRepository) {
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
        this.ingredientesService = ingredientesService;
        this.tipoPratoService = tipoPratoService;
        this.menuRepository = menuRepository;
    }

    // based on stock of ingredientes calculate stock of item
    public int CalculateStockByItemId(long itemId) throws Exception {
        // get ingredientes of item
        List<IngredienteQuantDTO> ingredientes = getIngredientesByItemId(itemId);
        if (ingredientes == null || ingredientes.isEmpty()) {
            System.err.println("O item não tem ingredientes definidos.");
            throw new Exception("O item não tem ingredientes definidos.");
        }
        int stock = Integer.MAX_VALUE; // Start with a large number
        for (IngredienteQuantDTO ingrediente : ingredientes) {
            long ingredienteId = ingrediente.getIngredienteId();
            int quantidade = ingrediente.getQuantidade();

            // Get stock of the ingredient
            int ingredienteStock = ingredientesService.getStockById(ingredienteId);
            if (ingredienteStock < quantidade) {
                System.err.println("O stock do ingrediente " + ingredienteId + " é insuficiente.");
                throw new Exception("O stock do ingrediente " + ingredienteId + " é insuficiente.");
            }

            // Calculate the stock of the item based on the ingredient stock
            int itemStock = ingredienteStock / quantidade;
            if (itemStock < stock) {
                stock = itemStock; // Update the minimum stock
            }
        }
        System.err.println("O stock do item " + itemId + " é: " + stock);
        return stock; // Return the calculated stock
    }

    public void SetCalculatedStockByItemId(long itemId) throws Exception {
        // Get the item by ID
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            System.err.println("O item com ID " + itemId + " não existe.");
            throw new Exception("O item com ID " + itemId + " não existe.");
        }
        int stock = CalculateStockByItemId(itemId);
        Item item = itemOpt.get();
        item.setStockAtual(stock);
        itemRepository.save(item);
        System.err.println("O stock do item " + itemId + " foi atualizado para: " + stock);
    }

    /* ---------------------------- CREATE ------------------------------ */
    public Item createItem(String nome,
            long tipoPratoId,
            float preco,
            List<IngredienteQuantDTO> ingredientes, // <-- alterado
            boolean eComposto,
            int stockAtual) throws Exception {

        if (itemRepository.findByNome(nome).isPresent()) {
            System.err.println("O item já existe.");
            throw new IllegalArgumentException("O item já existe.");
        }

        for (IngredienteQuantDTO ingrediente : ingredientes) {
            if (!ingredientesService.doesIngredienteExist(ingrediente.getIngredienteId())) {
                System.err.println("O ingrediente com ID " + ingrediente.getIngredienteId() + " não existe.");
                throw new IllegalArgumentException(
                        "O ingrediente com ID " + ingrediente.getIngredienteId() + " não existe.");
            }
        }

        if (!tipoPratoService.checkTipoPratoExists(tipoPratoId)) {
            System.err.println("O tipo de prato com ID " + tipoPratoId + " não existe.");
            throw new IllegalArgumentException("O tipo de prato com ID " + tipoPratoId + " não existe.");
        }

        Item item = new Item();
        item.setNome(nome);
        item.setTipoPrato_id(tipoPratoId);
        item.setPreco(preco);
        item.seteComposto(eComposto);
        item.setStockAtual(stockAtual);

        try {
            String json = objectMapper.writeValueAsString(ingredientes);
            item.setIngredientesJson(json);
        } catch (JsonProcessingException e) {
            // erro de serialização — não grava
            System.err.println("Falha a serializar ingredientes: " + e.getMessage());
            throw new Exception("Falha a serializar ingredientes: " + e.getMessage());
        }

        itemRepository.save(item);
        System.err.println("Item adicionado com sucesso.");
        return item; // sucesso
    }

    /* ---------------------------- READ ------------------------------- */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> getByPratoId(long tipoPratoId) {
        return itemRepository.findByTipoPratoId(tipoPratoId);
    }

    /* ---------------------------- UPDATE ----------------------------- */
    public Item editItem(long id,
            String nome,
            long tipoPratoId,
            float preco,
            List<IngredienteQuantDTO> ingredientes, // <-- alterado
            boolean eComposto,
            int stockAtual) throws Exception {

        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            System.err.println("O item não existe.");
            throw new IllegalArgumentException("O item não existe.");
        }

        for (IngredienteQuantDTO ingrediente : ingredientes) {
            if (!ingredientesService.doesIngredienteExist(ingrediente.getIngredienteId())) {
                System.err.println("O ingrediente com ID " + ingrediente.getIngredienteId() + " não existe.");
                throw new IllegalArgumentException(
                        "O ingrediente com ID " + ingrediente.getIngredienteId() + " não existe.");
            }
        }

        if (!tipoPratoService.checkTipoPratoExists(tipoPratoId)) {
            System.err.println("O tipo de prato com ID " + tipoPratoId + " não existe.");
            throw new IllegalArgumentException("O tipo de prato com ID " + tipoPratoId + " não existe.");
        }

        Item item = itemOpt.get();
        item.setNome(nome);
        item.setTipoPrato_id(tipoPratoId);
        item.setPreco(preco);
        item.seteComposto(eComposto);
        item.setStockAtual(stockAtual);

        try {
            String json = objectMapper.writeValueAsString(ingredientes);
            item.setIngredientesJson(json);
        } catch (JsonProcessingException e) {
            System.err.println("Falha a serializar ingredientes: " + e.getMessage());
            throw new Exception("Falha a serializar ingredientes: " + e.getMessage());
        }

        itemRepository.save(item);
        System.err.println("Item editado com sucesso.");
        return item; // sucesso
    }

    /* ---------------------------- DELETE ----------------------------- */
    public boolean deleteItem(long id) {
        if (!itemRepository.existsById(id)) {
            System.err.println("O item não existe.");
            return false;
        }

        // Check if item is part of any menu
        List<Menu> menusContainingItem = findMenusUsingItem(id);
        if (!menusContainingItem.isEmpty()) {
            StringBuilder menuNames = new StringBuilder();
            for (int i = 0; i < menusContainingItem.size(); i++) {
                if (i > 0) {
                    menuNames.append(", ");
                }
                menuNames.append("'").append(menusContainingItem.get(i).getNome()).append("'");
            }
            String errorMessage = "Não é possível remover o item pois está incluído nos seguintes menus: " + menuNames;
            System.err.println(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        itemRepository.deleteById(id);
        System.err.println("Item eliminado com sucesso.");
        return true;
    }

    public Optional<Item> getItemById(long id) {
        return itemRepository.findById(id);
    }

    public List<IngredienteQuantDTO> getIngredientesByItemId(long id) {
        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            String json = item.getIngredientesJson();
            try {
                return objectMapper.readValue(json,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, IngredienteQuantDTO.class));
            } catch (JsonProcessingException e) {
                System.err.println("Falha a deserializar ingredientes: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public boolean doesItemExist(long id) {
        return itemRepository.existsById(id);
    }

    public Item getById(long id) {
        if (itemRepository.existsById(id)) {
            return itemRepository.findById(id).get();
        }
        return null;
    }

    /**
     * Checks if an ingredient is used in any items
     * 
     * @param ingredienteId The ID of the ingredient to check
     * @return List of items that use the ingredient
     */
    public List<Item> findItemsUsingIngredient(long ingredienteId) {
        List<Item> itemsUsingIngredient = new ArrayList<>();
        List<Item> allItems = itemRepository.findAll();

        for (Item item : allItems) {
            List<IngredienteQuantDTO> ingredientes = getIngredientesByItemId(item.getId());
            if (ingredientes != null) {
                for (IngredienteQuantDTO ingrediente : ingredientes) {
                    if (ingrediente.getIngredienteId() == ingredienteId) {
                        itemsUsingIngredient.add(item);
                        break;
                    }
                }
            }
        }

        return itemsUsingIngredient;
    }

    /**
     * Checks if an item is used in any menus
     * 
     * @param itemId The ID of the item to check
     * @return List of menus that contain the item
     */
    public List<Menu> findMenusUsingItem(long itemId) {
        List<Menu> menusUsingItem = new ArrayList<>();
        List<Menu> allMenus = menuRepository.findAll();

        for (Menu menu : allMenus) {
            long[] itemIds = menu.getItems_id();
            if (itemIds != null) {
                for (long id : itemIds) {
                    if (id == itemId) {
                        menusUsingItem.add(menu);
                        break;
                    }
                }
            }
        }

        return menusUsingItem;
    }

    public List<Item> getItemsByIds(long[] ids) {
        List<Item> items = new ArrayList<>();
        for (Long id : ids) {
            Optional<Item> itemOpt = itemRepository.findById(id);
            if (itemOpt.isPresent()) {
                items.add(itemOpt.get());
            }
        }
        return items;
    }
}
