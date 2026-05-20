package main.patterns.decorator;

/**
 * Component Interface - Decorator Design Pattern (Structural)
 * 
 * Defines the common interface for both concrete products and decorators.
 * Named "ProductComponent" to avoid conflict with the Product model class.
 * 
 * This is the "Component" role in the Decorator pattern.
 * Both ConcreteProduct and all Decorators implement this interface.
 * 
 * SOLID: Interface Segregation Principle - minimal, focused interface.
 */
public interface ProductComponent {
    /**
     * @return Product name (may include decorator descriptions)
     */
    String getName();

    /**
     * @return Product description with applied decorations
     */
    String getDescription();

    /**
     * @return Calculated price after all decorations are applied
     */
    double getPrice();

    /**
     * @return true if free shipping is applied via decorator
     */
    boolean hasFreeShipping();
}
