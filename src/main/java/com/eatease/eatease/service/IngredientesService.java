package com.eatease.eatease.service;

import com.eatease.eatease.model.Ingredientes;
import com.eatease.eatease.model.Item;
import com.eatease.eatease.repository.IngredientesRepository;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredientesService {

    private final IngredientesRepository ingredientesRepository;
    private final UnidadeMedidaService unidadeMedidaService;
    private final ItemService itemService;

    public IngredientesService(IngredientesRepository ingredientesRepository,
            UnidadeMedidaService unidadeMedidaService,
            @Lazy ItemService itemService) {
        this.ingredientesRepository = ingredientesRepository;
        this.unidadeMedidaService = unidadeMedidaService;
        this.itemService = itemService;
    }

    public Ingredientes createIngredientes(String nome, int stock, int stock_min, String unidade) throws Exception {
        // Verifica se a unidade de medida existe
        Long unidade_id = unidadeMedidaService.getUnidadeMedidaId(unidade);
        if (unidade_id == null || unidade_id == -1) {
            System.err.println("A unidade de medida não existe.");
            throw new IllegalArgumentException("A unidade de medida não existe.");
        }

        if (ingredientesRepository.findByNome(nome).isEmpty()) {
            Ingredientes ingredientes = new Ingredientes();
            ingredientes.setNome(nome);
            ingredientes.setStock(stock); // use provided stock instead of stock_min
            ingredientes.setStock_min(stock_min);
            ingredientes.setUnidade_id(unidade_id);
            ingredientesRepository.save(ingredientes);
            System.err.println("Ingrediente adicionado com sucesso.");
            return ingredientes;
        } else {
            System.err.println("O ingredientes já existe.");
            throw new IllegalArgumentException("O ingredientes já existe.");
        }
    }

    public List<Ingredientes> getAllIngredientes() {
        return ingredientesRepository.findAll();
    }

    public Ingredientes getIngredienteById(long id) {
        return ingredientesRepository.findById(id).orElse(null);
    }

    public Ingredientes updateIngredientes(long id, String nome, int stock, int stock_min, String unidade)
            throws Exception {
        // Verifica se a unidade de medida existe
        long unidade_id = unidadeMedidaService.getUnidadeMedidaId(unidade);
        if (unidade_id == -1) {
            System.err.println("A unidade de medida não existe.");
            throw new IllegalArgumentException("A unidade de medida não existe.");
        }

        Ingredientes ingredientes = ingredientesRepository.findById(id).orElse(null);
        if (ingredientes != null) {
            ingredientes.setNome(nome);
            ingredientes.setStock(stock);
            ingredientes.setStock_min(stock_min);
            ingredientes.setUnidade_id(unidade_id);
            ingredientesRepository.save(ingredientes);
            System.err.println("Ingrediente atualizado com sucesso.");
            return ingredientes;
        } else {
            System.err.println("O ingrediente não existe.");
            throw new IllegalArgumentException("O ingrediente não existe.");
        }
    }

    public boolean deleteIngredientes(long id) throws IllegalArgumentException {
        Ingredientes ingredientes = ingredientesRepository.findById(id).orElse(null);
        if (ingredientes != null) {
            // Check if the ingredient is being used in any items
            List<Item> itemsUsingIngredient = itemService.findItemsUsingIngredient(id);
            if (!itemsUsingIngredient.isEmpty()) {
                StringBuilder itemNames = new StringBuilder();
                for (int i = 0; i < itemsUsingIngredient.size(); i++) {
                    if (i > 0) {
                        itemNames.append(", ");
                    }
                    itemNames.append("'").append(itemsUsingIngredient.get(i).getNome()).append("'");
                }
                String errorMessage = "Não é possível remover o ingrediente pois está a ser utilizado nos seguintes itens: "
                        + itemNames;
                System.err.println(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            ingredientesRepository.delete(ingredientes);
            System.err.println("Ingrediente removido com sucesso.");
            return true;
        } else {
            System.err.println("O ingrediente não existe.");
            return false;
        }
    }

    public boolean addStock(long id, int stock) {
        Ingredientes ingredientes = ingredientesRepository.findById(id).orElse(null);
        if (ingredientes != null) {
            ingredientes.setStock(ingredientes.getStock() + stock);
            ingredientesRepository.save(ingredientes);
            System.err.println("Stock adicionado com sucesso.");
            return true;
        } else {
            System.err.println("O ingrediente não existe.");
            return false;
        }
    }

    /**
     * Removes specified quantity from ingredient stock with transaction support
     * to maintain data consistency and prevent race conditions
     */
    @Transactional
    public boolean removeStock(long id, int stock) {
        Ingredientes ingredientes = ingredientesRepository.findById(id).orElse(null);
        if (ingredientes != null) {
            if (ingredientes.getStock() - stock < 0) {
                System.err.println("Stock insuficiente.");
                return false;
            }
            ingredientes.setStock(ingredientes.getStock() - stock);
            ingredientesRepository.save(ingredientes);
            System.err.println("Stock removido com sucesso.");
            return true;
        } else {
            System.err.println("O ingrediente não existe.");
            return false;
        }
    }

    public boolean doesIngredienteExist(long id) {
        return ingredientesRepository.existsById(id);
    }

    /**
     * Checks if an ingredient has enough stock for the required quantity
     * Read-only to optimize database access
     */
    @Transactional(readOnly = true)
    public boolean temStockSuficiente(long id, int stock) {
        Ingredientes ingredientes = ingredientesRepository.findById(id).orElse(null);
        if (ingredientes != null) {
            return ingredientes.getStock() >= stock;
        } else {
            System.err.println("O ingrediente não existe.");
            return false;
        }
    }

    public int getStockById(long id) {
        Ingredientes ingredientes = ingredientesRepository.findById(id).orElse(null);
        if (ingredientes != null) {
            return ingredientes.getStock();
        } else {
            System.err.println("O ingrediente não existe.");
            return -1; // Indicating that the ingredient does not exist
        }
    }
}
