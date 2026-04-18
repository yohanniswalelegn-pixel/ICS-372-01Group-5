package com.brewbite.factory;

import com.brewbite.model.menu.Beverage;
import com.brewbite.model.menu.BeverageSize;
import com.brewbite.model.menu.Customization;
import com.brewbite.model.menu.MenuItem;
import com.brewbite.model.menu.Pastry;
import com.brewbite.model.inventory.IngredientRequirement;

import java.util.List;

/**
 * Factory Method Pattern implementation for creating MenuItem instances.
 *
 * This factory centralises all construction logic for menu items,
 * ensuring that every item is created consistently with correct
 * ingredient requirements and default customizations.
 *
 * Responsibility: Construct fully-configured MenuItem objects so that
 * no other class needs to know the construction details. This supports
 * the Single Responsibility Principle — callers ask for an item and get
 * it back ready to use.
 *
 * Usage:
 *   MenuItem latte = MenuItemFactory.createLatte();
 *   MenuItem muffin = MenuItemFactory.createMuffin("Blueberry");
 */
public class MenuItemFactory {

    // ── Private constructor — utility class, not instantiated ────────────────
    private MenuItemFactory() {}

    // ════════════════════════════════════════════════════════════════════════
    // COFFEE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Creates a standard Latte.
     * Ingredients: 18g coffee beans, 200ml milk.
     */
    public static Beverage createLatte() {
        Beverage b = new Beverage("Latte", "Coffee", 4.50);
        b.setIngredientRequirements(List.of(
            new IngredientRequirement("Coffee Beans", 18),
            new IngredientRequirement("Milk",         200)
        ));
        addStandardCoffeeCustomizations(b);
        return b;
    }

    /**
     * Creates a Cappuccino.
     * Ingredients: 18g coffee beans, 120ml milk.
     */
    public static Beverage createCappuccino() {
        Beverage b = new Beverage("Cappuccino", "Coffee", 4.25);
        b.setIngredientRequirements(List.of(
            new IngredientRequirement("Coffee Beans", 18),
            new IngredientRequirement("Milk",         120)
        ));
        addStandardCoffeeCustomizations(b);
        return b;
    }

    /**
     * Creates an Espresso.
     * Ingredients: 14g coffee beans (no milk).
     */
    public static Beverage createEspresso() {
        Beverage b = new Beverage("Espresso", "Coffee", 3.00);
        b.setIngredientRequirements(List.of(
            new IngredientRequirement("Coffee Beans", 14)
        ));
        b.addCustomization(new Customization("Extra Shot",      0.75));
        b.addCustomization(new Customization("Decaf",           0.00));
        b.addCustomization(new Customization("Sugar-Free Syrup",0.50));
        return b;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TEA
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Creates a Green Tea.
     * Ingredients: 5g tea leaves, 250ml water.
     */
    public static Beverage createGreenTea() {
        Beverage b = new Beverage("Green Tea", "Tea", 3.00);
        b.setIngredientRequirements(List.of(
            new IngredientRequirement("Tea Leaves", 5),
            new IngredientRequirement("Water",      250)
        ));
        addStandardTeaCustomizations(b);
        return b;
    }

    /**
     * Creates a Black Tea.
     * Ingredients: 5g tea leaves, 250ml water.
     */
    public static Beverage createBlackTea() {
        Beverage b = new Beverage("Black Tea", "Tea", 3.00);
        b.setIngredientRequirements(List.of(
            new IngredientRequirement("Tea Leaves", 5),
            new IngredientRequirement("Water",      250)
        ));
        addStandardTeaCustomizations(b);
        return b;
    }

    /**
     * Creates a Herbal Tea.
     * Ingredients: 5g herbal blend, 250ml water.
     */
    public static Beverage createHerbalTea() {
        Beverage b = new Beverage("Herbal Tea", "Tea", 3.25);
        b.setIngredientRequirements(List.of(
            new IngredientRequirement("Herbal Blend", 5),
            new IngredientRequirement("Water",        250)
        ));
        addStandardTeaCustomizations(b);
        return b;
    }

    // ════════════════════════════════════════════════════════════════════════
    // PASTRIES
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Creates a Croissant of the specified variant.
     *
     * @param variant "Butter" or "Chocolate"
     */
    public static Pastry createCroissant(String variant) {
        Pastry p = new Pastry(variant + " Croissant", "Croissant", variant, 3.50);
        p.setIngredientRequirements(List.of(
            new IngredientRequirement("Flour",  80),
            new IngredientRequirement("Butter", 30)
        ));
        return p;
    }

    /**
     * Creates a Muffin of the specified variant.
     *
     * @param variant "Blueberry" or "Chocolate Chip"
     */
    public static Pastry createMuffin(String variant) {
        Pastry p = new Pastry(variant + " Muffin", "Muffin", variant, 3.25);
        p.setIngredientRequirements(List.of(
            new IngredientRequirement("Flour",           90),
            new IngredientRequirement("Sugar",           40),
            new IngredientRequirement("Chocolate Chips", variant.contains("Chocolate") ? 30 : 0)
        ));
        return p;
    }

    /**
     * Creates a Cookie of the specified variant.
     *
     * @param variant "Chocolate Chip" or "Oatmeal Raisin"
     */
    public static Pastry createCookie(String variant) {
        Pastry p = new Pastry(variant + " Cookie", "Cookie", variant, 2.75);
        p.setIngredientRequirements(List.of(
            new IngredientRequirement("Flour",           60),
            new IngredientRequirement("Sugar",           30),
            new IngredientRequirement("Butter",          20),
            new IngredientRequirement("Chocolate Chips", variant.contains("Chocolate") ? 20 : 0)
        ));
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    // GENERIC FACTORY METHOD (used by persistence to rebuild from JSON type)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Generic factory method that dispatches to the correct typed creator
     * based on a name string. Used when loading catalog data from JSON.
     *
     * @param name the canonical menu item name (e.g. "Latte", "Green Tea")
     * @return the fully configured MenuItem, or null if name is unrecognised
     */
    public static MenuItem createByName(String name) {
        return switch (name) {
            case "Latte"          -> createLatte();
            case "Cappuccino"     -> createCappuccino();
            case "Espresso"       -> createEspresso();
            case "Green Tea"      -> createGreenTea();
            case "Black Tea"      -> createBlackTea();
            case "Herbal Tea"     -> createHerbalTea();
            case "Butter Croissant"         -> createCroissant("Butter");
            case "Chocolate Croissant"      -> createCroissant("Chocolate");
            case "Blueberry Muffin"         -> createMuffin("Blueberry");
            case "Chocolate Chip Muffin"    -> createMuffin("Chocolate Chip");
            case "Chocolate Chip Cookie"    -> createCookie("Chocolate Chip");
            case "Oatmeal Raisin Cookie"    -> createCookie("Oatmeal Raisin");
            default -> null;
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private static void addStandardCoffeeCustomizations(Beverage b) {
        b.addCustomization(new Customization("Extra Shot",      0.75));
        b.addCustomization(new Customization("Decaf",           0.00));
        b.addCustomization(new Customization("Oat Milk",        0.60));
        b.addCustomization(new Customization("Almond Milk",     0.60));
        b.addCustomization(new Customization("Sugar-Free Syrup",0.50));
    }

    private static void addStandardTeaCustomizations(Beverage b) {
        b.addCustomization(new Customization("Oat Milk",        0.60));
        b.addCustomization(new Customization("Almond Milk",     0.60));
        b.addCustomization(new Customization("Sugar-Free Syrup",0.50));
        b.addCustomization(new Customization("Extra Honey",     0.25));
    }
}
