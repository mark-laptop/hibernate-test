package ru.ndg;

import ru.ndg.model.Consumer;
import ru.ndg.model.Product;
import ru.ndg.util.DBUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;

public class Application {

    public static void main(String[] args) {

        initializeDataDB(6);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            while (true) {
                System.out.println("Выберите команду:");
                showMainCommand();

                try {

                    int result = Integer.parseInt(reader.readLine());
                    if (result == 5) break;

                    switch (result) {
                        case 1:
                            showAllProductForConsumer();
                            break;
                        case 2:
                            showAllConsumerForProduct();
                            break;
                        case 3:
                            showAllConsumer();
                            break;
                        case 4:
                            showAllProduct();
                            break;
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Введено не корретное значение.");
                }

            }
        } catch(IOException ignored) {

        } finally {
            DBUtil.getEntityManagerFactory().close();
        }

    }

    private static void showMainCommand() {
        StringBuilder sb = new StringBuilder()
                .append("1. Вывести список продуктов указанного покупателя.")
                .append("\n")
                .append("2. Вывести список покупателей кто купил указанный продукт.")
                .append("\n")
                .append("3. Вывести список покупателей.")
                .append("\n")
                .append("4. Вывести список продуктов.")
                .append("\n")
                .append("5. Выйти из приложения.");
        System.out.println(sb.toString());
    }

    private static void showAllProductForConsumer() {
        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        Consumer consumer = entityManager.find(Consumer.class, 1L);
        TypedQuery<Product> query = entityManager.createQuery("SELECT p FROM Product p INNER JOIN p.consumers c WHERE c.id = :consumerId", Product.class);
        List<Product> resultList = query.setParameter("consumerId", consumer.getId()).getResultList();
        resultList.forEach(System.out::println);
    }

    private static void showAllConsumerForProduct() {
        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        Product product = entityManager.find(Product.class, 2L);
        TypedQuery<Consumer> query = entityManager.createQuery("SELECT c FROM Consumer c INNER JOIN c.products p WHERE p.id = :productId", Consumer.class);
        List<Consumer> resultList = query.setParameter("productId", product.getId()).getResultList();
        resultList.forEach(System.out::println);
    }

    private static void showAllConsumer() {
        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        List<Consumer> consumers = entityManager.createQuery("FROM Consumer", Consumer.class).getResultList();
        consumers.forEach(System.out::println);
        entityManager.close();
    }

    private static void showAllProduct() {
        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        List<Product> products = entityManager.createQuery("FROM Product", Product.class).getResultList();
        products.forEach(System.out::println);
        entityManager.close();
    }

    private static void initializeDataDB(int count) {
        if (count < 0) return;

        createProducts(count);
        createConsumer(count);
        createProductForConsumer(count);
    }

    private static void createProducts(int count) {

        if (count < 0) return;

        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        entityManager.getTransaction().begin();

        for (long i = 1; i <= count; i++) {
            Product product = Product.builder()
                    .name("Product " + i)
                    .price(new BigDecimal(i + "000"))
                    .build();
            entityManager.persist(product);
        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private static void createConsumer(int count) {

        if (count < 0) return;

        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        entityManager.getTransaction().begin();

        for (long i = 1; i <= count; i++) {
            Consumer consumer = Consumer.builder()
                    .name("Consumer " + i)
                    .build();
            entityManager.persist(consumer);
        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private static void createProductForConsumer(int count) {

        if (count < 0) return;

        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        entityManager.getTransaction().begin();

        for (long i = 1; i <= count; i++) {
            Product product = entityManager.find(Product.class, i);
            if (product != null) {
                Consumer consumer = entityManager.find(Consumer.class, i);
                if (consumer != null) {
                    consumer.addProduct(product);
                    entityManager.merge(consumer);
                }
            }
        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
