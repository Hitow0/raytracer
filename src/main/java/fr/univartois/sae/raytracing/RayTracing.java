package fr.univartois.sae.raytracing;

import fr.univartois.sae.raytracing.light.IStrategy;
import fr.univartois.sae.raytracing.object.AObject;
import fr.univartois.sae.raytracing.object.Sphere;
import fr.univartois.sae.raytracing.scene.Scene;
import fr.univartois.sae.raytracing.scene.SceneBuilder;
import fr.univartois.sae.raytracing.triplet.Color;
import fr.univartois.sae.raytracing.triplet.Point;
import fr.univartois.sae.raytracing.triplet.Triplet;
import fr.univartois.sae.raytracing.triplet.Vector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Class RayTracing to generate an image from a scene
 * @author leo.denis
 */
public class RayTracing {

        /*
        charger la scène
    — pour chaque pixel (i, j) de l’image à générer
        — calculer le vecteur unitaire d qui représente un rayon allant de l’œil lookF rom au centre du
        pixel (i, j)
        — rechercher le point p = lookF rom + d × t d’intersection le plus proche (t minimal) avec un
        objet de la scène
        — si p existe alors calculer sa couleur
        — sinon utiliser du noir
        — peindre le pixel (i, j) avec la couleur adéquate
        — sauvegarder l’image
         */

    private Scene scene;

    private BufferedImage image;

    private IStrategy strategy;

    /**
     * Create an image from a scene
     * @param scene the scene
     */
    public RayTracing(Scene scene, IStrategy strategy){
        this.scene=scene;
        Vector lookFrom=new Vector(scene.getCamera().get(0));
        Vector looKAt=new Vector(scene.getCamera().get(1));
        Vector up = new Vector(scene.getCamera().get(2));
        Vector w=lookFrom.subtraction(looKAt.getTriplet()).scalarMultiplication((1/(lookFrom.subtraction(looKAt.getTriplet()).norm())));
        Vector u = (up.vectorProduct(w.getTriplet())).scalarMultiplication(1/up.vectorProduct(w.getTriplet()).norm());
        Vector v = (w.vectorProduct(u.getTriplet())).scalarMultiplication(1/w.vectorProduct(u.getTriplet()).norm());
        double fovr = (scene.getFov()*Math.PI)/180;
        double realHeight = 2*Math.tan(fovr/2);
        double pixelHeight = realHeight / scene.getHeight();
        double realWidth = scene.getWidth() * pixelHeight;
        double pixelWidth = realWidth / scene.getWidth();
        Color[][] colors = new Color[scene.getWidth()][scene.getHeight()];
        for(int i=0;i<scene.getWidth();i++){
            for(int j=0;j<scene.getHeight();j++) {
                double a = (-(realWidth / 2) + ((i + 0.5) * pixelWidth));
                double b = ((realHeight / 2) - ((j + 0.5) * pixelHeight));
                double t = -1;
                Vector d = new Vector((((u.scalarMultiplication(a)).addition(v.scalarMultiplication(b).getTriplet())).subtraction(w.getTriplet())).scalarMultiplication(1 / (((u.scalarMultiplication(a).addition(v.scalarMultiplication(b).getTriplet()).subtraction(w.getTriplet())).getTriplet()).norm())).getTriplet());
                Color color = new Color(0.0, 0.0, 0.0);
                int o =-1;
                int index =0;
                for (AObject object : scene.getObjects()) {
                    if (object instanceof Sphere) {
                        // IL VA FALLOIR UTILISER LES FONCTIONS MODELES ICI !!!
                        double tmp;
                        double t2;
                        double tb = ((lookFrom.subtraction(((Sphere) object).getCoordinate().getTriplet())).scalarMultiplication(2)).scalarProduct(d.getTriplet());
                        double tc = (lookFrom.subtraction(((Sphere) object).getCoordinate().getTriplet())).scalarProduct(lookFrom.subtraction(((Sphere) object).getCoordinate().getTriplet()).getTriplet()) - (Math.pow(((Sphere) object).getRadius(), 2));
                        double delta = Math.pow(tb, 2) - (4 * tc);
                        if (delta == 0) {
                            t = -tb / 2;
                        } else if (delta > 0) {
                            tmp = (-tb + Math.sqrt(delta)) / 2;
                            t2 = (-tb - Math.sqrt(delta)) / 2;
                            if (t2 > 0) {
                                tmp = t2;
                            } else if (t < 0) {
                                tmp = -1;
                            }
                            if ((tmp<=t) || (t==-1)) {
                                t = tmp;
                                o=index;
                            }
                        }
                        index++;
                        if (t >= 0) {
                            Point p = new Point(lookFrom.addition(d.getTriplet().scalarMultiplication(t)).getTriplet());
                            color = strategy.modelMethod((Sphere) scene.getObjects().get(o), o, p, scene);
                        }

                        // ICI !!!!
                    } else {
                        throw new UnsupportedOperationException();
                    }
//                    if (color.getTriplet().getY()<0.5 && color.getTriplet().getY()!=0.0) {
//                        System.out.println("r");
//                    }
                    colors[i][j] = new Color(color.getTriplet());
                }





            }
        }
        for(int i=0;i<scene.getWidth();i++){
            for(int j=0;j<scene.getHeight();j++) {
                Triplet t = colors[i][j].getTriplet();
                if ( t.getY()<0.9 && t.getY()!=0.0)
                    System.out.println(colors[i][j]);
            }
        }
        createImage(colors);
    }

    /**
     * Create an image with a 2D array of colors
     * @param colors a 2D array of colors
     */
    public void createImage(Color[][] colors) {
        try {
            image = new BufferedImage(scene.getWidth(),scene.getHeight(), 1);
            for (int i=0; i< scene.getWidth(); i++) {
                for (int j=0; j< scene.getHeight();j++) {
                    image.setRGB(i,j,new java.awt.Color((int) (colors[i][j].getTriplet().getX()*255), (int) (colors[i][j].getTriplet().getY()*255), (int) (colors[i][j].getTriplet().getZ()*255)).getRGB());
                }
            }
            File outputfile = new File(scene.getOutput());
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
