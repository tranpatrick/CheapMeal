package util.bdTools;	

import java.sql.Timestamp;

import org.hibernate.Session;

import util.BCrypt;
import util.ServiceTools;
import util.hibernate.HibernateUtil;
import util.hibernate.model.Profils;
import util.hibernate.model.Sessions;
import util.hibernate.model.Utilisateurs;


public class RequeteStatic {

	/**
	 * Supprime la session d'un utilisateur grace a cle de session
	 * @param cle la cle de session
	 */
	public static void supprimerSessionAvecCle(String cle){
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		s.createQuery("delete from Sessions where cleSession = :cleSession")
						.setParameter("cleSession", cle)
						.executeUpdate();
	
		s.getTransaction().commit();
		s.close();
	}

	
	/**
	 * Verifie si un login est libre ou non
	 * @param login le login a tester
	 * @return true si le login est disponible, false sinon
	 */
	public static boolean isLoginDisponible(String login)  {
		
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Utilisateurs user =(Utilisateurs) s.createQuery("from Utilisateurs u where login = :login")
					.setParameter("login", login)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		return user == null;
	}

	/**
	 * Verifie si le couple login/mdp est valide
	 * @param login le nom d'utilisateur
	 * @param mdp le mot de passe de l'utilisateur
	 * @return true si le couple existe, false sinon
	 */
	public static boolean checkIdentifiantsValide(String login,String mdp)  {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Utilisateurs user = (Utilisateurs) s.createQuery("from Utilisateurs u where login = :login")
					.setParameter("login", login)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		boolean bonmdp = false;
		if(user != null)
			bonmdp = BCrypt.checkpw(mdp, user.getMdp());
		
		return (user != null) && bonmdp;
	}

	/**
	 * Verifie si une cle de session pour un utilisateur existe
	 * @param login le nom d'utilisateur
	 * @return true si une cle de session existe, false sinon 
	 */
	public static boolean isSessionCree(String login)  {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Sessions session =(Sessions) s.createQuery("select s from Utilisateurs u,Sessions s where u.login = :login and u.id = s.idSession")
					.setParameter("login", login)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		return session != null;
	}

	/**
	 * Met a jour la date d'expiration de la cle de session d'un
	 * utilisateur en utilisant son login (pour la connexion)
	 * @param login le nom d'utilisateur
	 */
	public static void updateDateExpirationAvecLogin(String login)  {
		Session s = HibernateUtil.getSessionFactory().openSession();
		Timestamp time = new Timestamp(System.currentTimeMillis()+30*60*1000);
		s.beginTransaction();
		s.createSQLQuery("update SESSIONS s, UTILISATEURS u set s.dateExpiration = :time where s.idSession = u.id and u.login = :u_login")
						.setParameter("u_login", login)
						.setParameter("time", time)
						.executeUpdate();
		s.getTransaction().commit();
		s.close();
	}
	
	/**
	 * Permet de recuperer la cle de session d'un utilisateur a partir
	 * de son login
	 * @param login le nom d'utilisateur
	 * @return la cle de session associee a login
	 */
	public static String recupererTokenAvecLogin(String login)  {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		String session_token =(String) s.createQuery("select s.cleSession from Utilisateurs u,Sessions s where u.login = :login and u.id = s.idSession")
					.setParameter("login", login)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		return session_token;
	}

	/**
	 * Permet de creer une creer cle de session pour l'utilisateur donne
	 * a partir de son login
	 * @param login le login de l'utilisateur
	 * @return la cle de session cree
	 */
	public static String createSessionFromLogin(String login) {
		int id = obtenirIdAvecLogin(login);
		Timestamp time = new Timestamp(System.currentTimeMillis()+30*60*1000);
		String cle = ServiceTools.createKey();
		
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Utilisateurs user = (Utilisateurs) s.load(Utilisateurs.class, id);
		Sessions s1 = new Sessions(id, cle, time);
		s1.setUtilisateur(user);
		s.save(s1);
		s.getTransaction().commit();
		s.close();
		return cle;
	}

	/**
	 * Met a jour la date d'expiration de la cle de session d'un
	 * utilisateur a partir de sa cle (utilisation de la cle)
	 * @param cle la cle session utilisateur
	 */
	public static void updateDateExpirationAvecCle(String cle)  {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		s.createSQLQuery("update SESSIONS set dateExpiration=date_add(now(), INTERVAL 30 MINUTE) where cleSession = :cle")
						.setParameter("cle", cle)
						.executeUpdate();
		s.getTransaction().commit();
		s.close();
	}
	
	/**
	 * Permet d'ajouter un utilisateur (creation de compte), cree egalement
	 * son profil
	 * @param login le nom d'utilisateur
	 * @param mdp le mot de passe
	 * @param nom le nom
	 * @param prenom le prenom
	 * @param email l'adresse mail
	 * @return l'identifiant id avec lequel l'utilisateur a ete ajoute dans
	 * dans la base de donnees
	 */
	public static Integer ajoutUtilisateur(String login, String mdp, String nom, String prenom, String email) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Integer id = (Integer) s.save(new Utilisateurs(login, mdp, prenom, nom, email));
		s.getTransaction().commit();
		ajouterProfil(id);
		s.close();
		return id;
	}
	
	/**
	 * Permet de creer automatiquement le profil d'un utilisateur
	 * a la creation de son compte, sa biographie est vide
	 * @param id l'identifiant de l'utilisateur
	 */
	private static void ajouterProfil(int id) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Utilisateurs user = (Utilisateurs) s.load(Utilisateurs.class, id);
		Profils p1 = new Profils(id, null);
		p1.setUtilisateur(user);
		s.save(p1);
		s.getTransaction().commit();
		s.close();
	}

	/**
	 * Permet de verifier si un email est disponible
	 * @param email l'addresse mail a tester
	 * @return true si l'adresse mail est disponible, false sinon
	 */
	public static boolean isEmailDisponible(String email) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		String u_mail =(String) s.createQuery("select u.mail from Utilisateurs u where u.mail= :mail")
					.setParameter("mail", email)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		return u_mail == null;
	}
	
	/**
	 * Permet d'obtenir l'identifiant d'un utilisateur a partir 
	 * de son nom d'utilisateur
	 * @param login le nom d'utilisateur
	 * @return l'id de l'utilisateur si l'utilisateur existe, -1 sinon
	 */
	public static int obtenirIdAvecLogin(String login) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Integer u_id =(Integer) s.createQuery("select u.id from Utilisateurs u where u.login= :login")
					.setParameter("login", login)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		if(u_id == null)
			return -1;
		else
			return u_id;
	}
	
	/**
	 * Permet de recuperer le mot de passe d'un utilisateur 
	 * a partir de son login
	 * @param login le nom d'utilisateur
	 * @return le mot de passe de l'utilisateur s'il existe
	 */
	public static String obtenirMdpAvecLogin(String login) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		String u_mdp =(String) s.createQuery("select u.mdp from Utilisateurs u where u.login= :login")
					.setParameter("login", login)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		return u_mdp;
	}
	
	/**
	 * Permet de changer le mot de passe d'un utilisateur avec son id
	 * @param id l'identifiant d'un utilisateur
	 * @param mdp le nouveau mot de passe
	 */
	public static void changerMdpAvecId(int id, String mdp) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		s.createQuery("update Utilisateurs u set u.mdp = :mdp where u.id = :id")
					.setParameter("mdp", BCrypt.hashpw(mdp, BCrypt.gensalt()))
					.setParameter("id", id)
					.executeUpdate();
		s.getTransaction().commit();
		s.close();
	}
	/**
	 * Permet d'ajouter une bio � un profil
	 * @param id de l'utilisateur 
	 * @param bio le texte de la bio
	 */
	public static void ajouterBioProfil(int id, String bio) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		s.createQuery("update Profils p set p.bio = :bio where p.id = :id")
		.setParameter("bio", bio)
		.setParameter("id", id)
		.executeUpdate();
		s.getTransaction().commit();
		s.close();
		
	}
	
	/**
	 * Permet de recuperer l'identifiant d'un utilisateur a partir de
	 * sa cle de session
	 * @param cle la cle de session
	 * @return l'id de l'utilisateur si sa cle de session existe,
	 * false sinon
	 */
	public static int obtenirIdSessionAvecCle(String cle) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Integer s_id =(Integer) s.createQuery("select s.idSession from Sessions s where s.cleSession = :cle")
					.setParameter("cle", cle)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		if(s_id == null)
			return -1;
		else
			return s_id;
	}


	/**
	 * Permet de changer l'adresse mail d'un utilisateur avec son id
	 * @param id l'identifiant d'un utilisateur
	 * @param mail la nouvelle adresse mail
	 */
	public static void changerEmailAvecId(int id, String mail) {
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		s.createQuery("update Utilisateurs u set u.mail = :mail where u.id = :id")
					.setParameter("mail", mail)
					.setParameter("id", id)
					.executeUpdate();
		s.getTransaction().commit();
		s.close();
	}
	
	/**
	 * Permet d'obtenir le login avec le mail d'un utilisateur
	 * @param email l'adresse email de l'utilisateur
	 * @return String le nom d'utilisateur
	 */
	public static String obtenirLoginAvecMail(String email){
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		String u_mail =(String) s.createQuery("select u.login from Utilisateurs u where u.mail= :email")
					.setParameter("email", email)
					.uniqueResult();
		s.getTransaction().commit();
		s.close();
		return u_mail;
	}
	/**
	 * Permet d'obtenir la Session (correspond a la table SESSION en SQL) � partir de la cle
	 * @param key la cle session utilisateur
	 * @return Sessions l'objet Sessions (model Hibernate) correspondant a la session
	 */
	public static Sessions obtenirSession(String key){
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Sessions res = (Sessions) s.createQuery("select s from Sessions s where s.cleSession = :cle")
				.setParameter("cle", key)
				.uniqueResult();
		s.getTransaction().commit();
		s.close();
		return res;
	}
	/**
	 * Permet d'obtenir un utilisateur avec ses identifiants
	 * @param id l'id de l'utilisateur
	 * @param login le nom d'utilisateur
	 * @return Utilisateurs l'objet Utilisateur (model Hibernate) correspondant a l'utilisateur
	 */
	public static Utilisateurs obtenirUtilisateur(Integer id, String login){
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		Utilisateurs res = null;
		if(id != null && login != null){
			res = (Utilisateurs) s.createQuery("select u from Utilisateurs u where u.id = :id and u.login = :login")
					.setParameter("id", id)
					.setParameter("login", login)
					.uniqueResult();
		}else if(id == null){
			res = (Utilisateurs) s.createQuery("select u from Utilisateurs u where u.login = :login")
					.setParameter("login", login)
					.uniqueResult();
		}else{
			res = (Utilisateurs) s.createQuery("select u from Utilisateurs u where u.id = :id")
					.setParameter("id", id)
					.uniqueResult();
		}
		s.getTransaction().commit();
		s.close();
		/* Si pas de parametre ou resultat n'existe pas, alors le resultat renvoye est null */
		return res;
	}

	/**
	 * Permet d'obtyenir la bio d'un utilisateur avec son login
	 * @param login le nom d'utilisateur
	 * @return String la chaine de caracteres correspondant a la bio de l'utilisateur
	 */
	public static String recupBio(String login) {
		Utilisateurs u = obtenirUtilisateur(null, login);
		Session s = HibernateUtil.getSessionFactory().openSession();
		s.beginTransaction();
		String p = (String) s.createQuery("select p.bio from Profils p where idProfil = :idProfil ")
					.setParameter("idProfil",u.getId())
					.uniqueResult();
		s.close();
		return p;
	}

}