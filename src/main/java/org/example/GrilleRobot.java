package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class GrilleRobot {

    static class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    public static void main(String[] args) {
        Point robot = new Point(0, 0);
        Point entrepot = new Point(3, 3);
        Point croix1 = new Point(2, 2);
        Point croix2 = new Point(4, 4);

        Scanner scanner = new Scanner(System.in);
        boolean enJeu = true;

        System.out.println("=== DÉBUT DU JEU ===");
        afficherGrille(robot, croix1, croix2, entrepot);

        while (enJeu) {
            System.out.print("\nAction (NORD, SUD, EST, OUEST, AUTO, QUITTER) : ");
            String commande = scanner.nextLine().trim().toUpperCase();

            if (commande.equals("QUITTER")) {
                enJeu = false;
                System.out.println("Fermeture du programme...");
            } else if (commande.equals("AUTO")) {
                executerDijkstra(robot, entrepot, croix1, croix2);
                // L'affichage est maintenant géré étape par étape dans la méthode Dijkstra
            } else {
                boolean deplacementValide = deplacerRobot(robot, commande, croix1, croix2);
                if (deplacementValide) {
                    afficherGrille(robot, croix1, croix2, entrepot);
                }
            }
        }

        scanner.close();
    }

    public static void executerDijkstra(Point source, Point target, Point c1, Point c2) {
        Map<Point, Integer> dist = new HashMap<>();
        Map<Point, Point> prev = new HashMap<>();
        List<Point> Q = new ArrayList<>();

        int taille = 5;

        // 1. Initialisation
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                Point v = new Point(x, y);
                dist.put(v, 9999);
                prev.put(v, null);
                Q.add(v);
            }
        }

        dist.put(source, 0);

        // 2. Boucle principale de Dijkstra
        while (!Q.isEmpty()) {

            Point u = Q.get(0);
            for (Point p : Q) {
                if (dist.get(p) < dist.get(u)) {
                    u = p;
                }
            }

            Q.remove(u);

            if (u.equals(target) || dist.get(u) == 9999) {
                break;
            }

            Point[] voisins = {
                    new Point(u.x, u.y - 1), // NORD
                    new Point(u.x, u.y + 1), // SUD
                    new Point(u.x + 1, u.y), // EST
                    new Point(u.x - 1, u.y)  // OUEST
            };

            for (Point v : voisins) {
                if (v.x >= 0 && v.x < taille && v.y >= 0 && v.y < taille) {
                    if (!v.equals(c1) && !v.equals(c2) && Q.contains(v)) {

                        int alt = dist.get(u) + 1;

                        if (alt < dist.get(v)) {
                            dist.put(v, alt);
                            prev.put(v, u);
                        }
                    }
                }
            }
        }

        List<Point> chemin = new ArrayList<>();
        Point courant = target;

        if (prev.get(courant) == null && !courant.equals(source)) {
            System.out.println("/!\\ Aucun chemin trouvé vers l'entrepôt !");
            return;
        }

        while (courant != null) {
            chemin.add(courant);
            courant = prev.get(courant);
        }

        Collections.reverse(chemin);

        System.out.println("\n-> Distance jusqu'à l'entrepôt : " + dist.get(target) + " cases.");
        System.out.println("-> Chemin calculé par Dijkstra : " + chemin);

        chemin.remove(0);

        for (Point etape : chemin) {
            source.x = etape.x;
            source.y = etape.y;

            System.out.println("\n--- Déplacement automatique en " + source + " ---");
            afficherGrille(source, c1, c2, target);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static boolean deplacerRobot(Point robot, String direction, Point c1, Point c2) {
        int taille = 5;
        int futurX = robot.x;
        int futurY = robot.y;

        switch (direction) {
            case "NORD": futurY--; break;
            case "SUD": futurY++; break;
            case "EST": futurX++; break;
            case "OUEST": futurX--; break;
            default:
                System.out.println("/!\\ Direction inconnue.");
                return false;
        }

        if (futurX >= 0 && futurX < taille && futurY >= 0 && futurY < taille) {
            Point futurPos = new Point(futurX, futurY);
            if (futurPos.equals(c1) || futurPos.equals(c2)) {
                System.out.println("/!\\ Action refusée : Un obstacle (X) bloque le passage !");
                return false;
            }
            robot.x = futurX;
            robot.y = futurY;
            System.out.println("-> Déplacement réussi vers le " + direction);
        } else {
            System.out.println("/!\\ Action refusée : Vous foncez dans un mur au " + direction + " !");
        }

        return true;
    }

    public static void afficherGrille(Point robot, Point c1, Point c2, Point entrepot) {
        int taille = 5;

        System.out.println("\n    0   1   2   3   4  (X)");
        System.out.println("  +---+---+---+---+---+");

        for (int y = 0; y < taille; y++) {
            System.out.print(y + " |");
            for (int x = 0; x < taille; x++) {
                if (x == robot.x && y == robot.y) {
                    System.out.print(" R |");
                } else if (x == c1.x && y == c1.y) {
                    System.out.print(" X |");
                } else if (x == c2.x && y == c2.y) {
                    System.out.print(" X |");
                } else if (x == entrepot.x && y == entrepot.y) {
                    System.out.print(" E |");
                } else {
                    System.out.print("   |");
                }
            }
            System.out.println("\n  +---+---+---+---+---+");
        }
        System.out.println("(Y)");
    }
}