package com.eatease.eatease.service;

import com.eatease.eatease.dto.IngredienteQuantDTO;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.model.Menu;
import com.eatease.eatease.repository.ItemRepository;
import com.eatease.eatease.repository.MenuRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper; // Jackson
    private final IngredientesService ingredientesService;
    private final TipoPratoService tipoPratoService;
    private final MenuRepository menuRepository;

    private final String uploadDir = "uploads/items/";

    public ItemService(ItemRepository itemRepository, ObjectMapper objectMapper,
            IngredientesService ingredientesService, TipoPratoService tipoPratoService,
            MenuRepository menuRepository) {
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
        this.ingredientesService = ingredientesService;
        this.tipoPratoService = tipoPratoService;
        this.menuRepository = menuRepository;
    }

    private String savePhoto(MultipartFile file, String itemName) throws Exception {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = itemName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
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
                return 0; // If any ingredient stock is less than required quantity, return 0
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
        item.setFoto(null); // Inicialmente sem foto

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
        // update all stocks before returning
        List<Item> items = itemRepository.findAll();
        for (Item item : items) {
            try {
                SetCalculatedStockByItemId(item.getId());
            } catch (Exception e) {
                System.err.println("Erro ao calcular stock do item " + item.getId() + ": " + e.getMessage());
            }
        }
        System.err.println("Todos os itens foram atualizados com sucesso.");
        return items;
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
        item.setFoto(null);

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
        if (!itemRepository.existsById(id)) {
            System.err.println("O item com ID " + id + " não existe.");
            return Optional.empty();
        }
        System.err.println("Item encontrado com sucesso.");
        // update stock before returning
        try {
            SetCalculatedStockByItemId(id);
        } catch (Exception e) {
            System.err.println("Erro ao calcular stock do item " + id + ": " + e.getMessage());
        }
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

    public List<Item> getByPratoId(long tipoPratoId) throws IllegalArgumentException {
        if (!tipoPratoService.checkTipoPratoExists(tipoPratoId)) {
            System.err.println("O tipo de prato com ID " + tipoPratoId + " não existe.");
            throw new IllegalArgumentException("O tipo de prato com ID " + tipoPratoId + " não existe.");
        }
        return itemRepository.findByTipoPratoId(tipoPratoId);
    }

    public Item getByIdNoUpdate(long id) {
        if (itemRepository.existsById(id)) {
            return itemRepository.findById(id).get();
        }
        return null;
    }

    public Item getByIdUpdate(long id) {
        if (itemRepository.existsById(id)) {
            // update stock before returning
            try {
                SetCalculatedStockByItemId(id);
            } catch (Exception e) {
                System.err.println("Erro ao calcular stock do item " + id + ": " + e.getMessage());
            }
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
                // update stock before adding
                try {
                    SetCalculatedStockByItemId(id);
                } catch (Exception e) {
                    System.err.println("Erro ao calcular stock do item " + id + ": " + e.getMessage());
                }
                items.add(itemOpt.get());
            }
        }
        return items;
    }

    // Method to update photo for existing item
    public String updateItemPhoto(long itemId, MultipartFile file) throws Exception {
        // Check if item exists
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            System.err.println("O item com ID " + itemId + " não existe.");
            throw new IllegalArgumentException("O item com ID " + itemId + " não existe.");
        }

        Item item = itemOpt.get();

        // Delete old photo if exists
        if (item.getFoto() != null && !item.getFoto().isEmpty()) {
            deletePhotoFile(item.getFoto());
        }

        // Save new photo
        String filename = savePhoto(file, item.getNome());
        item.setFoto(filename);
        itemRepository.save(item);

        System.err.println("Foto do item " + itemId + " atualizada com sucesso: " + filename);
        return filename;
    }

    // Method to delete photo file from filesystem
    private void deletePhotoFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.err.println("Foto anterior eliminada: " + filename);
            }
        } catch (Exception e) {
            System.err.println("Erro ao eliminar foto anterior: " + e.getMessage());
        }
    }

    // Method to delete photo from existing item
    public boolean deleteItemPhoto(long itemId) throws Exception {
        // Check if item exists
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            System.err.println("O item com ID " + itemId + " não existe.");
            return false;
        }

        Item item = itemOpt.get();

        // Check if item has photo
        if (item.getFoto() == null || item.getFoto().isEmpty()) {
            System.err.println("O item com ID " + itemId + " não tem foto.");
            return false;
        }

        // Delete photo file
        deletePhotoFile(item.getFoto());

        // Update item record
        item.setFoto(null);
        itemRepository.save(item);

        System.err.println("Foto do item " + itemId + " eliminada com sucesso.");
        return true;
    }

    // Method to get photo URL for an item
    public String getPhotoUrl(long itemId, String baseUrl) {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            if (item.getFoto() != null && !item.getFoto().isEmpty()) {
                return baseUrl + "/uploads/items/" + item.getFoto();
            }
        }
        return null;
    }

    // Method to check if photo file exists
    public boolean photoExists(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }
}
