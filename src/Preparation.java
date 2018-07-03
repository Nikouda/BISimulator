import java.util.ArrayList;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class Preparation extends Thread {
	
	
	ArrayList<Boucle> boucles = new ArrayList<Boucle>();
	ArrayList<String> references;
	
	public Preparation()
	{
		//G�n�ration des r�f�rences
		references = genererReference();
		
		ArrayList<String> referencesParGare = new ArrayList<String>();
		
		int refX = 0;
		int refMaxParGare = 100;
		
		boucles.add(new Boucle());
		boucles.add(new Boucle());		
		
		System.out.println("Initialisation de la boucle N*1");
		
		//Boucle n*1
		for(int i = 1; i < 20; i++)
		{
			Gare gare = new Gare(i);
			
			//Ajoute les diff�rentes r�f�rences pour la gare
			for(int position = refX; position < i*refMaxParGare; position++)
			{
				gare.ref_produits.add(references.get(position));
			}
			refX += 100;
			
			boucles.get(0).ajouterGare(gare);
		}
		
		System.out.println("Initialisation de la boucle N*2");
		
		try
		{
			//Boucle n*2
			for(int i = 21; i < 40; i++)
			{
				Gare gare = new Gare(i);
				
				//Ajoute les diff�rentes r�f�rences pour la gare
				for(int position = refX; position < i*refMaxParGare; position++)
				{
					gare.ref_produits.add(references.get(position));
				}
				refX += 100;
				
				boucles.get(1).ajouterGare(gare);
			}
		}
		catch (Exception ex)
		{
			//Les references sont initialis�es
		}
		
		System.out.println("Pr�t !");
	}
	
	//THREAD
    @Override 
    public void run() { 

		ArrayList<Commande> commandes = recupererLesCommandes();
		
		
		for(Commande c : commandes)
		{
			System.out.println("Commande :");
			System.out.println("REF: " + c.reference);
			System.out.println("Nombre de carton : " + c.getNombreColis());
			
			for(Colis co : c.colis)
			{
				System.out.println("\t- Nb produits : " + co.getNombreProduits());
				System.out.println("\t- Espace utilis� : " + co.espace_utilise);

				/*
				for(Produit p : co.produits)
				{
					System.out.println("\t \t- " + p.espace);
				}*/
			}
		}
		
    } 
	
	
	//Recupere la gare n*x
	public Gare getGare(int numero)
	{
		if(numero < 21)
		{
			return boucles.get(0).getGareNumero(numero);
		}
		else
		{
			return boucles.get(1).getGareNumero(numero);
		}		
	}
	
	//Recupere le numero d'une gare par rapport � sa ref
	public int getNumeroGare(String ref)
	{
		for(int i = 0; i < boucles.size(); i++)
		{
			for (Gare gare : boucles.get(i).gares)
			{
				if(gare.referenceEstDansLaGare(ref))
				{
					return gare.numero;
				}
			}
		}
		
		return 41;
	}
	
	//Generation des r�f�rences de chaque produit;
	private ArrayList<String> genererReference()
	{
		ArrayList<String> references = new ArrayList<String>();

		String[] nom = {"Acidofilo", "Bouteille cola", "Brazil pik", "Color Schtroummpf pik", "Langues acides", 
				"London pik", "Miami pik", "Pasta Basta", "Pasta frutta", "Sour snup", "Dragibus", "Carensac", 
				"Fraizibus", "Grain de millet", "Starmint", "Florent violette", "Kimono", "Pain Zan", "Rotella", 
				"Zano�d", "Fraise tagada", "Croco", "Chamallows", "Polka", "Banane", "Ourson", "Filament"};
		
		
		String[] couleur = {"Rouge", "Orange", "Jaune", "Vert", "Bleu", "Violet", "Noir", "Marron"};
		String[] variante = {"Acide", "Sucre", "Gelifie"};
		String[] texture = {"Mou", "Dur"};
		String[] conditionnement = {"Sachet", "Boite", "Echantillon"};
		

		for(int a = 0; a < nom.length; a++)
		{
			for(int b = 0; b < couleur.length; b++)
			{
				for(int c = 0; c < variante.length; c++)
				{
					for(int d = 0; d < texture.length; d++)
					{
						for(int e = 0; e < conditionnement.length; e++)
						{
							references.add(a  + "-" + b + "-" + c + "-" + d + "-" + e);
						}
					}
				}
			}
		}
		
		return references;
	}
	
	private ArrayList<Commande> recupererLesCommandes()
	{

		ArrayList<Commande> commandes = new ArrayList<Commande>();
		
		//Connexion � la base de donn�es
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://projet:projetb1@ds227171.mlab.com:27171/projetbi?retryWrites=true"));
		MongoDatabase db = client.getDatabase("projetbi");
		
		MongoCollection<Document> collection = db.getCollection("commandes");
		
		//Recuperation des commandes
		Bson filtre = Filters.eq("etat", "Preparation");
		for (Document commande : collection.find(filtre))
		{
			//System.out.println("");
			//System.out.println("ref_commande :" + commande.get("_id"));
			ArrayList<Document> lignes = (ArrayList<Document>)commande.get("lignesCommande");
			
			//Recuperation de l'id de la commande
			String id = ((ObjectId)commande.get("_id")).toString();
			Commande cmd = new Commande(id);
			

			//Insertion des produits dans les commandes
			for (Document ligne : lignes)
			{
				//System.out.println("\n\tProduit N*"+ cpt + " : ");
				//System.out.println("\tref : " + ligne.get("reference"));
				//System.out.println("\tConditionnement : " + ligne.get("conditionnement") + "\n");
				
				String ref = ligne.getString("reference");
				int quantite = ligne.getInteger("quantity");
				
				for(int x = 0; x < quantite; x++)
				{
					cmd.ajouterProduit(new Produit(ref, getNumeroGare(ref)));
				}
			}
			//System.out.println(cmd.produits.size());
			
			//On pr�pare le nombre de colis � envoyer
			cmd.preparerColis();
			
			//On ajoute les colis � la liste des comandes
			commandes.add(cmd);
		}
		
		System.out.println(commandes.size() + " commandes en attentes de pr�paration...");	
		client.close();	
		
		return commandes;
	}
}
