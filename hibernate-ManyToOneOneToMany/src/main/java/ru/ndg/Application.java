package ru.ndg;

import ru.ndg.model.Consumer;
import ru.ndg.model.Product;
import ru.ndg.model.Purchase;
import ru.ndg.util.DBUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Application {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public static void main(String[] args) {

        initializeDataDB(6);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            while (true) {
                System.out.println("Выберите команду:");
                showMainCommand();

                try {

                    int result = Integer.parseInt(reader.readLine());
                    if (result == 6) break;

                    switch (result) {
                        case 1:
                            showAllProductForConsumer(reader);
                            break;
                        case 2:
                            showAllConsumerForProduct(reader);
                            break;
                        case 3:
                            showAllConsumer();
                            break;
                        case 4:
                            showAllProduct();
                            break;
                        case 5:
                            showAllPurchase();
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
                .append("5. Вывести список покупок.")
                .append("\n")
                .append("6. Выйти из приложения.");
        System.out.println(sb.toString());
    }

    private static void showAllProductForConsumer(BufferedReader reader) {
        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        Consumer consumer = enterDataForConsumer(reader);
        if (consumer != null) {
            TypedQuery<Purchase> query = entityManager.createQuery(
                    "SELECT p FROM Purchase p INNER JOIN p.consumer c WHERE c.id = :consumerId"
                    , Purchase.class);
            List<Purchase> resultList = query.setParameter("consumerId", consumer.getId()).getResultList();
            resultList.forEach((purchase) -> System.out.println(String.format(
                    "Product: {id: %d, name: %s, price: %s}"
                    , purchase.getProduct().getId()
                    , purchase.getProduct().getName()
                    , purchase.getProduct().getPrice()
            )));
        } else {
            System.out.println("Покупатель не найден.");
        }
        entityManager.close();
    }

    private static void showAllConsumerForProduct(BufferedReader reader) {
        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        Product product = enterDataForProduct(reader);
        if (product != null) {
            TypedQuery<Purchase> query = entityManager.createQuery(
                    "SELECT p FROM Purchase p INNER JOIN p.product prod WHERE prod.id = :productId"
                    , Purchase.class);
            List<Purchase> resultList = query.setParameter("productId", product.getId()).getResultList();
            resultList.forEach((purchase) -> System.out.println(String.format(
                    "Consumer: {id: %d, name: %s}"
                    , purchase.getConsumer().getId()
                    , purchase.getConsumer().getName()
            )));
        } else {
            System.out.println("Продукт не найден.");
        }
        entityManager.close();
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

    private static void showAllPurchase() {
        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        List<Purchase> purchases = entityManager.createQuery("FROM Purchase", Purchase.class).getResultList();
        purchases.forEach((purchase) -> System.out.println(String.format(
                "Purchase: {id: %d, consumer: %s, product: %s, current price: %s, date purchase: %s}"
                , purchase.getId()
                , purchase.getConsumer().getName()
                , purchase.getProduct().getName()
                , purchase.getPricePurchaseDate()
                , dateFormat.format(purchase.getDatePurchase())
        )));
        entityManager.close();
    }

    private static Consumer enterDataForConsumer(BufferedReader reader) {
        Consumer consumer = null;
        try {
            System.out.println("Введите id покупателя.");
            String rawData = reader.readLine();
            long id = Long.parseLong(rawData);
            EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
            consumer = entityManager.find(Consumer.class, id);
            entityManager.close();
        } catch (IOException | NumberFormatException ignored) {}
        return consumer;
    }

    private static Product enterDataForProduct(BufferedReader reader) {
        Product product = null;
        try {
            System.out.println("Введите id продукта.");
            String rawData = reader.readLine();
            long id = Long.parseLong(rawData);
            EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
            product = entityManager.find(Product.class, id);
            entityManager.close();
        } catch (IOException | NumberFormatException ignored) {}
        return product;
    }

    private static void initializeDataDB(int count) {
        if (count < 0) return;

        createProducts(count);
        createConsumer(count);
        createPurchase(count);
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

    private static void createPurchase(int count) {

        if (count < 0) return;

        EntityManager entityManager = DBUtil.getEntityManagerFactory().createEntityManager();
        entityManager.getTransaction().begin();

        for (long i = 1; i <= count; i++) {
            Product product = entityManager.find(Product.class, i);
            if (product != null) {
                Consumer consumer = entityManager.find(Consumer.class, i);
                if (consumer != null) {
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(new Date());
                    instance.add(Calendar.DATE, (int)i);
                    Date date = instance.getTime();
                    Purchase purchase = Purchase.builder()
                            .consumer(consumer)
                            .product(product)
                            .pricePurchaseDate(product.getPrice())
                            .datePurchase(date)
                            .build();
                    entityManager.merge(purchase);
                }
            }
        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
