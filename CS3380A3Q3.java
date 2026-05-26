/**
 * Assignment: 3
 * Question: 3
 * Name: Izzy Elskamp
 * Student#: 7956885
 */

import java.sql.*;

import java.util.Scanner;

public class CS3380A3Q3 {
	static Connection connection;

	public static void main(String[] args) throws Exception {

		// startup sequence
		MyDatabase db = new MyDatabase();
		runConsole(db);

		System.out.println("Exiting...");
	}

	public static void runConsole(MyDatabase db) {

		Scanner console = new Scanner(System.in);
		System.out.print("Welcome! Type h for help. ");
		System.out.print("db > ");
		String line = console.nextLine();
		String[] parts;
		String arg = "";

		while (line != null && !line.equals("q")) {
			parts = line.split("\\s+");
			if (line.indexOf(" ") > 0)
				arg = line.substring(line.indexOf(" ")).trim();

			if (parts[0].equals("h"))
				printHelp();
			else if (parts[0].equals("mp")) {
				db.getMostPublishers();
			}

			else if (parts[0].equals("s")) {
				if (parts.length >= 2)
					db.nameSearch(arg);
				else
					System.out.println("Require an argument for this command");
			}

			else if (parts[0].equals("l")) {
				try {
					if (parts.length >= 2)
						db.lookupByID(arg);
					else
						System.out.println("Require an argument for this command");
				} catch (Exception e) {
					System.out.println("id must be an integer");
				}
			}

			else if (parts[0].equals("sell")) {
				try {
					if (parts.length >= 2)
						db.lookupWhoSells(arg);
					else
						System.out.println("Require an argument for this command");
				} catch (Exception e) {
					System.out.println("id must be an integer");
				}
			}

			else if (parts[0].equals("notsell")) {
				try {
					if (parts.length >= 2)
						db.whoDoesNotSell(arg);
					else
						System.out.println("Require an argument for this command");
				} catch (Exception e) {
					System.out.println("id must be an integer");
				}
			}

			else if (parts[0].equals("mc")) {
				db.mostCities();
			}

			else if (parts[0].equals("notread")) {
				db.ownBooks();
			}

			else if (parts[0].equals("all")) {
				db.readAll();
			}

			else if (parts[0].equals("mr")) {
				db.mostReadPerCountry();
			}

			else
				System.out.println("Read the help with h, or find help somewhere else.");

			System.out.print("db > ");
			line = console.nextLine();
		}

		console.close();
	}

	private static void printHelp() {
		System.out.println("Library database");
		System.out.println("Commands:");
		System.out.println("h - Get help");
		System.out.println("s <name> - Search for a name");
		System.out.println("l <id> - Search for a user by id");
		System.out.println("sell <author id> - Search for a stores that sell books by this id");
		System.out.println("notread - Books not read by its own author");
		System.out.println("all - Authors that have read all their own books");
		System.out.println("notsell <author id>  - list of stores that do not sell this author");
		System.out.println("mp - Authors with the most publishers");
		System.out.println("mc - Authors with books in the most cities");
		System.out.println("mr - Most read book by country");
		System.out.println("");

		System.out.println("q - Exit the program");

		System.out.println("---- end help ----- ");
	}

}

class MyDatabase {
	private Connection connection;

	public MyDatabase() {
		try {
			String url = "jdbc:sqlite:library.db";
			// create a connection to the database
			connection = DriverManager.getConnection(url);
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}

	}

	//1
	public void nameSearch(String name) {
        try
        {
            String sql = "SELECT id, first, last FROM people WHERE LOWER(first) LIKE ? OR LOWER(last) LIKE ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            String search = "%" + name.toLowerCase() + "%";
            statement.setString(1, search);
            statement.setString(2, search);
            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String first = resultSet.getString("first");
                String last = resultSet.getString("last");

                System.out.println("ID: [" + id + "] First: [" + first + "] Last: [" + last + "]");
                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

	//2
	public void lookupByID(String id) {
        try
        {
            String sql = "SELECT first, last, aid FROM people WHERE ? = id";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                String first = resultSet.getString("first");
                String last = resultSet.getString("last");
                int aid = resultSet.getInt("aid");
                if(aid == 0)
                {
                    aid = -1;
                }
                System.out.println("First: [" + first + "] Last: [" + last + "] Aid: [" + aid + "]");
                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

	//3
	public void lookupWhoSells(String id) {
        try
        {
            String sql = "SELECT store.name, COUNT(books.bid) AS books_num FROM store " +
                    "JOIN sells ON store.id = sells.sid JOIN publishers ON sells.pid = publishers.pid JOIN books ON publishers.pid = books.pid " +
                    "WHERE books.aid = ? GROUP BY store.id, store.name";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                String storeName = resultSet.getString("name");
                int booksNum = resultSet.getInt("books_num");

                System.out.println("Store: [" + storeName + "] Count: [" + booksNum + "]");
                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

	//4
	public void ownBooks() {
        try
        {
            String sql = "SELECT people.first, people.last, books.title FROM people " +
                    "JOIN books ON people.aid = books.aid " +
                    "WHERE books.bid NOT IN (SELECT read.bid FROM read WHERE read.id = people.id)";

            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                String first = resultSet.getString("first");
                String last = resultSet.getString("last");
                String title = resultSet.getString("title");

                System.out.println("First: [" + first + "] Last: [" + last + "] Title: [" + title + "]");
                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

	//5
	public void readAll() {
        try
        {
            String sql = "SELECT people.first, people.last FROM people " +
                    "WHERE NOT EXISTS (SELECT * FROM books " +
                    "WHERE books.aid = people.aid AND books.bid NOT IN " +
                    "(SELECT read.bid FROM read WHERE read.id = people.id))";

            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                String first = resultSet.getString("first");
                String last = resultSet.getString("last");

                System.out.println("First: [" + first + "] Last: [" + last + "]");
                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

	//6
	public void whoDoesNotSell(String id) {
        try
        {
            String sql = "SELECT store.name FROM store WHERE store.id NOT IN (" +
                    "SELECT DISTINCT sells.sid FROM sells JOIN books ON sells.pid = books.pid WHERE books.aid = ?)";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id);

            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                String storeName = resultSet.getString("name");

                System.out.println("Store: [" + storeName + "]");
                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

	//7
	public void getMostPublishers() {
        try
        {
            String sql = "SELECT people.first, people.last, COUNT(DISTINCT books.pid) AS publisher_count " +
                    "FROM people JOIN books ON people.aid = books.aid GROUP BY people.id, people.first, people.last " +
                    "ORDER BY publisher_count DESC LIMIT 5";

            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                String first = resultSet.getString("first");
                String last = resultSet.getString("last");
                int publisherCount = resultSet.getInt("publisher_count");

                System.out.println("First: [" + first + "] Last: [" + last + "] PublisherCount: [" + publisherCount + "]");

                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

	//8
	public void mostCities() {
        try
        {
            String sql = "SELECT people.first, people.last, COUNT(DISTINCT city.cid) AS city_count " +
                    "FROM people JOIN books ON people.aid = books.aid JOIN publishers ON books.pid = publishers.pid " +
                    "JOIN sells ON publishers.pid = sells.pid JOIN store ON sells.sid = store.id JOIN city ON store.cid = city.cid " +
                    "GROUP BY people.id, people.first, people.last ORDER BY city_count DESC LIMIT 5";

            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                String first = resultSet.getString("first");
                String last = resultSet.getString("last");
                int  cityCount = resultSet.getInt("city_count");

                System.out.println("First: [" + first + "] Last: [" + last + "] CityCount: [" + cityCount+ "]");

                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

	//9
	public void mostReadPerCountry() {
        try
        {
            String sql = "SELECT city.country, books.title FROM books " +
                    "JOIN publishers ON books.pid = publishers.pid JOIN city ON publishers.cid = city.cid " +
                    "WHERE books.bid = (SELECT booksInner.bid FROM books booksInner " +
                    "JOIN publishers pubInner ON booksInner.pid = pubInner.pid " +
                    "JOIN city cityInner ON pubInner.cid = cityInner.cid " +
                    "JOIN read readInner ON booksInner.bid = readInner.bid " +
                    "WHERE cityInner.country = city.country GROUP BY booksInner.bid " +
                    "ORDER BY COUNT(readInner.id) DESC LIMIT 1) " +
                    "GROUP BY city.country";

            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();

            boolean foundResults = false;
            while (resultSet.next())
            {
                String country = resultSet.getString("country");
                String title = resultSet.getString("title");

                System.out.println("Country: [" + country + "] Title: [" + title + "]");

                foundResults = true;
            }

            if(!foundResults)
            {
                System.out.println("Could not find results.");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace(System.out);
        }
	}

}
