/*******************************************************************************
 * Copyright (c) 2010 Torsten Zesch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     Torsten Zesch - initial API and implementation
 *     Oliver Ferschke
 *     Samy Ateia
 ******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.api;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.api.hibernate.PageDAO;
import de.tudarmstadt.ukp.wikipedia.api.sweble.PlainTextConverter;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp. @author Oliver Ferschkekp.wikipedia.util.UnmodifiableArraySet;

/**
 * Represents a Wikipedia article page.
 *
 * @author zesch
 *
 */
// Adapter class for hidding hibernate session management from the user.
public class Page
	implements WikiConstants
{
	private final Wikipedia wiki;

	private final PageDAO pageDAO;

	// The hibernatePage that is represented by this WikiAPI page.
	// The indirection is necessary to shield the
//	// If it differs from the page's name, we searched for a redirect.
//	private String searchString;

	// If we search for a redirect, the corresponding page is delivered transparently.
	// In that case, isRedirect is set to true, to indicate that.
	// Note: The page itself is _not_ a redirect, it is just a page.
	private boolean isRedirect = false;

	/**
	 * Creates a page object.
	 *
	 * @param wiki
	 *            The wikipedia object.
	 * @param id
	 *            The hibernate id of the page.
	 * @throws WikiApiException
	 */
	protected Page(Wikipedia wiki, long id)
		throws WikiApiException
	{
		this.wiki = wiki;
		this.pageDAO = new PageDAO(wiki);
		fetchByHibernateId(id);
	}

	/**
	 * Creates a page object.
	 *
	 * @param wiki
	 *            The wikipedia object.
	 * @param pageID
	 *            The pageID of the page.
	 * @throws WikiApiException
	 */
	protected Page(Wikipedia wiki, int pageID)
		throws WikiApiException
	{
		this.wiki = wiki;
		this.pageDAO = new PageDAO(wiki);
		fetchByPageId(pageID);
	}

	/**
	 * Creates a page object.
	 *
	 * @param wiki
	 *            The wikipedia object.
	 * @param pName
	 *            The name of the page.
	 * this(wiki, pName, false);
	}
	
	/**
     * Creates a page object.
     *
     * @param wiki
     *            The wikipedia object.
     * @param pName
     *            The name of the page.
     * @param useExactTitle
     *            Whether to use the exact title or try to guess the correct wiki-style title.
     * @throws WikiApiException
     */
    public Page(Wikipedia wiki, String pName, boolean useExactTitle)
        throws WikiApiException
    {
        if (pName == null || pName.length() == 0) {
            throw new WikiPageNotFoundException();
        }
        this.wiki = wiki;
        this.pageDAO = new PageDAO(wiki);
        Title pageTitle = new Title(pName);
        fetchByTitle(pageTitle, useExactTitle);
    ;
		this.pageDAO = new PageDAO(wiki);
		Title pageTitle = new Title(pName);
		fetchByTitle(pageTitle);
	}

	/**
	 * Creates a Page object from an already retrieved hibernate Page
	 *
	 * @param wiki
	 *            The wikipedia object.
	 * @param id
	 *            The hibernate id of the page.
	 * @param hibernatePage
	 * 			  The {@code api.hibernatePage} that has already been retrieved
	 * @throws WikiApiException
	 */
	protected Page(Wikipedia wiki, long id,
			de.tudarmstadt.ukp.wikipedia.api.hibernate.Page hibernatePage)
		throws WikiApiException
	{
		this.wiki = wiki;
		this.pageDAO = new PageDAO(wiki);
		this.hibernatePage = hibernatePage;
	}

	/**
	 * @throws WikiApiException
	 * @see de.tudarmstadt.ukp.wikipedia.api.Page#Page(long)
	 */
	private void fetchByHibernateId(long id)
		throws WikiApiException
	{
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
        hibernatePage = pageDAO.findById(id);
        session.getTransaction().commit();

        if (hibernatePage == null) {
            throw new WikiPageNotFoundException("No page with id " + id + " was found.");
        }
	}

	private void fetchByPageId(int pageID)
		throws WikiApiException
	{
        Session session = this.wiki.__getHibernateSession();
        session.beginTransaction();
		hibernatePage:id").setInteger("id", pageID).uniqueResult(__getHibernateSession();
        session.beginTransaction();
        hibernatePage = pageDAO.findById(id);
        session.getTransaction().commit();

        if (hibernatePage == null) {
             throw new WikiPageNotFoundException("No page with page id " + pageID + " was found.");
        }
	}

	/**
	 * CAUTION: Only returns 1 result, even if several results are p, boolean useExactTitle)
		throws WikiApiException
	{
		String searchString = pTitle.getPlainTitle();
		if (!useExactTitle) {
		    searchString = pTitle.getWikiStyleTitle();
		}
chByTitle(Title pTitle)
		throws WikiApiException
	{
		String searchString = pTitle.getWikiStyleTitle();
		Session session;
		session = this.wiki.__getHibernateSession();
		session.beginTransaction();
		Integer pageId = (:pagetitle LIMIT 1")
				.setString("pagetitle", searchString).uniqueResult();ID from PageMapLine as pml where pml.name = ? LIMIT 1")
				.setString(0, searchString).uniqueResult();

        session.getTransaction().commit();

        if (pageId == null) {
			throw new WikiP			
        if (searchString != null&&redirect searchString differs from the page's name.
        if (searchString != null) {
            if (!s    /*
                 * WORKAROUND
                 * in our page is a redirect to a discussion page, we might not retrieve the target discussion page as expected but rather the article associated with the target discussion page
                 * we check this here and re-retrieve the correct page.
                 * this error should be avoided by keeping the namespace information in the database
                 * This fix has been provided by Shiri Dori-Hacohen and is discussed in the Google Group under https://groups.google.com/forum/#!topic/jwpl/2nlr55yp87I/discussion
                 */
                if (searchString.startsWith(DISCUSSION_PREFIX) && !getTitle().getRawTitleText().startsWith(DISCUSSION_PREFIX)) {
                	try {
                		fetchByTitle(new Title(DISCUSSION_PREFIX + getTitle().getRawTitleText()), useExactTitle);
                	} catch (WikiPageNotFoundException e) {
                		throw new WikiPageNotFoundException("No page with name " + DISCUSSION_PREFIX + getTitle().getRawTitleText() + " was found.");
                	}
                }
        }
	}

	/**
	 * @return Returns the id.
	 */
	protected long __getId()
	{
		long id = hibernatePage.getId();
		return id;
	}

	/**
	 * @return Returns a unique page id.
	 */
	public int getPageId()
	{
		int id = hibernatePage.getPageIdhibernatePage, LockMode.NONE);
		int id = hibernatePage.getPageId();
//		session.getTransaction().commit();
		return id;
	}

	/**
	 * Returns a set of categories that this page belongs to.
	 *
	 * @return The a set of categories that this page belongs to.
	 */
	public Set<Category> .__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);
		Set<Integer> tmp = new UnmodifiableArraySet<Integer>(hibernatePage.getCategories());
		session.getTransaction().commit();

		Set<Category> categories = new HashSet<Category>();
		for (int pageID : tmp) {
			categories.add(wiki.getCategory(pageID));
		}

		return categories;
	}

	/**
	 * This is a more efficient shortcut for writing "getCategories().size()", as that would require
	 * to load all the categories first.
	 *
	 * @return The number of categories.
	 */
	public int getNumberOfCategories()
	{
		BigInteger nrOfCategories = new BigInteger("0");

		long id = __getId();
		Session session = wiki.__getHibernateSession();
		 session.beginTransaction();
:pageid")
				.setLong("pageid"ue = session
				.createSQLQuery("select count(pages) from page_categories where id = ?")
				.setLong(0, id).uniqueResult();
		 session.getTransaction().commit();

		if (returnValue != null) {
			nrOfCategories = (BigInteger) returnValue;
		}
		return nrOfCategories.intValue();
	}

	/**
	 * Returns the set of pages that have a link pointing to this page. <b>Warning:</b> Do not use
	 * this for getting the number of inlinks with getInlinks().size(). This is too slow. Use
	 * getNumberOfInlinks() instead.
	 *
	 * @return The set of pages that have a link pointing to this page.
	 * @throws WikiApiException
	 */
	public Se.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);
		// Have to copy links here since getPage later will close the session.
		Set<Integer> pageIDs = new UnmodifiableArraySet<Integer>(hibernatePage.getInLinks());
		session.getTransaction().commit();

		Set<Page> pages = new HashSet<Page>();
		for (int pageID : pageIDs) {
			try {
				pages.add(wiki.getPage(pageID));
			}
			catch (WikiApiException e) {
				// Silently ignore if a page could not be found
				// There may be inlinks that do not come from an existing page.
				continue;
			}
		}

		return pages;
	}

	/**
	 * This is a more efficient shortcut for writing "getInlinks().size()", as that would require to
	 * load all the inlinks first.
	 *
	 * @return The number of inlinks.
	 */
	public int getNumberOfInlinks()
	{
		BigInteger nrOfInlinks = new BigInteger("0");

		long id = __getId();
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
		Object re:piid")
				.setLong("piid"e = session
				.createSQLQuery("select count(outLinks) from page_outlinks where id = ?")
				.setLong(0, id).uniqueResult();
		session.getTransaction().commit();

		if (returnValue != null) {
			nrOfInlinks = (BigInteger) returnValue;
		}
		return nrOfInlinks.intValue();
	}

	/**
	 * The result set may also contain links from non-existing pages. It is in the responsibility of
	 * the user to check whether the page exists.
	 *
	 * @return Returns the IDs of the inLinks of this page.
	 */
	public Set<Integer> getInlinkIDs()
	{
		Set<Integer> tmpSet = new HashSet<Integer>();

		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);

		tmpSet.addAll(hibernatePage.getInLinks());

		session.getTransaction().commit();

		return tmpSet;
	}

	/**
	 * Returns the set of pages that are linked from this page. Outlinks in a page might also point
	 * to non-existing pages. They are not included in the result set. <b>Warning:</b> Do not use
	 * this for getting the number of outlinks with getOutlinks().size(). This is too slow. Use
	 * getNumberOfOutlinks() instead.
	 *
	 * @return The set of pages that are linked from this page.
	 * @throws WikiApiException
	 */
	public Set<Page> getOutlinks()
	{
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);
		// Have to copy links here since getPage later will close the session.
		Set<Integer> tmpSet = new UnmodifiableArraySet<Integer>(hibernatePage.getOutLinks());
		session.getTransaction().commit();

		Set<Page> pages = new HashSet<Page>();
		for (int pageID : tmpSet) {
			try {
				pages.add(wiki.getPage(pageID));
			}
			catch (WikiApiException e) {
				// Silently ignore if a page could not be found.
				// There may be outlinks pointing to non-existing pages.
			}
		}
		return pages;
	}

	/**
	 * This is a more efficient shortcut for writing "getOutlinks().size()", as that would require
	 * to load all the outlinks first.
	 *
	 * @return The number of outlinks.
	 */
	public int getNumberOfOutlinks()
	{
		BigInteger nrOfOutlinks = new BigInteger("0");

		long id = __getId();
		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
	:id")
				.setLong("id"e = session
				.createSQLQuery("select count(outLinks) from page_outlinks where id = ?")
				.setLong(0, id).uniqueResult();
		session.getTransaction().commit();

		if (returnValue != null) {
			nrOfOutlinks = (BigInteger) returnValue;
		}
		return nrOfOutlinks.intValue();
	}

	/**
	 * The result set may also contain links from non-existing pages. It is in the responsibility of
	 * the user to check whether the page exists.
	 *
	 * @return Returns the IDs of the outLinks of this page.
	 */
	public Set<Integer> getOutlinkIDs()
	{
		Set<Integer> tmpSet = new HashSet<Integer>();

		Session session = wiki.__getHibernateSession();
		session.beginTransaction();
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);

		tmpSet.addAll(hibernatePage.getOutLinks());

		session.getTransaction().commit();
		return tmpSet;
	}

	/**
	 * Returns the title of the page.
	 *
	 * @return The title of the page.
	 * @throws WikiTitleParsingException
	 */
	public Title getTitle()
		throws WikiT.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		String name = hibernatePage.getName();
		session.getTransaction().commit();
		Title title = new Title(name);
		return title;
	}

	/**
	 * Returns the set of strings that are redirects to this page.
	 *
	 * @return The set of redirect strings.
	 */
	public Set<St.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		session.buildLockRequest(LockOptions.NONE).lock(hibernatePage);
		Set<String> tmpSet = new HashSet<String>(hibernatePage.getRedirects());
		session.getTransaction().commit();
		return tmpSet;
	}

	/**
	 * Returns the text of the page with media wiki markup.
	 *
	 * @return The text of the page with media wiki markup.
	 */
	pub.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		String text = hibernatePage.getText();
		session.getTransaction().commit();

		// Normalize strings read from the DB to use "\n" for all line breaks.
		StringBuilder sb = new StringBuilder(text);
		int t = 0;
		boolean seenLineBreak = false;
		char breakQue = ' ';
		for (int s = 0; s < sb.length(); s++) {
			char c = sb.charAt(s);
			boolean isLineBreak = c == '\n' || c == '\r';
			if (isLineBreak) {
				if (seenLineBreak && !(c == breakQue)) {
					// This is a Windows or Mac line ending. Ignoring the second char
					seenLineBreak = false;
					continue;
				}
				else {
					// Linebreak character that we cannot ignore
					seenLineBreak = true;
					breakQue = c;
				}
			}
			else {
				// Reset linebreak state
				seenLineBreak = false;
			}

			// Character needs to be copied
			sb.setCharAt(t, isLineBreak ? '\n' : c);
			t++;
		}
		sb.setLength(t);

		return sb.toString();
	}

	/**
	 * Returns true, if the page is a disambiguation page, false otherwise.
	 *
	 * @return True, if the page is a disambiguation page, false otherwise.
	 */
	public boolea.__getHibernateSession();
		session.beginTransaction();
//		session.lock(hibernatePage, LockMode.NONE);
		boolean isDisambiguation = hibernatePage.getIsDisambiguation();
		session.getTransaction().commit();
		return isDisambiguation;
	}

	/**
	 * Returns true, if the page was returned by querying a redirect string, false otherwise.
	 *
	 * @return True, if the page was returned by querying a redirect string, false otherwise.
	 */
	public boolean isRedirect()
	{
		return isRedirect;
	}

    /**
     * @return True, if the page is a discussion page.
     * @throws WikiTitleParsingException
     */
    public boolean isDiscussion() throws WikiTitleParsingExcept<p>Returns the Wikipedia article as plain text using the SwebleParser with
	 * a SimpleWikiConfiguration and the PlainTextConverter. <br/>
	 * If you have different needs regarding the plain text, you can use
	 * getParsedPage(Visitor v) and provide your own Sweble-Visitor. Examples
	 * are in the <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package
	 * or on http://www.sweble.org </p>
	 *
	 * <p>Alternatively, use Page.getText() to return the Wikipedia article
	 * with all Wiki markup. You can then use the old JWPL MediaWiki parser for
	 * creating a plain text version. The JWPL parser is now located in a
	 * separate project <code>de.tudarmstad.ukp.wikipedia.parser</code>.
	 * Please refer to the JWPL Google Code project page for further reference.</p>
	 *
	 * @return The plain text of a Wikipedia articleicle
	 * with all Wiki markup.
	 *
	 * @return The plain text of a Wikipedia article without all//Configure the PlainTextConverter for plain text parsing
		return (String) parsePage(new PlainTextConverter());
	}

	/**
	 * Parses the page with the Sweble parser using a SimpleWikiConfiguration
	 * and the provided visitor. For further information about the visitor
	 * concept, look at the examples in the
	 * <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package, or on
	 * <code>http://www.sweble.org</code> or on the JWPL Google Code project
	 * page.
	 *
	 * @return the parsed page. The actual return type depends on the provided
	 *         visitor. You have to cast the return type according to the return
	 *         type of the go() method of your visitor.
	 * @throws WikiApiException
	 */
	public Object parsePage(AstVisitor v) throws WikiApiException
	{
		// Use the provided visitor to parse the page
		return v.go(getCompiledPage().getPage());
	}

	/**
	 * Returns CompiledPage produced by the SWEBLE parser using the
	 * SimpleWikiConfiguration.
	 *
	 * @return the parsed page
	 * @throws WikiApiException
	 */
	public CompiledPage getCompiledPage() throws WikiApiException
	{
		CompiledPage cp;
		try{
			SimpleWikiConfiguration config = new SimpleWikiConfiguration(SWEBLE_CONFIG);

			PageTitle pageTitle = PageTitle.make(config, this.getTitle().toString());
			PageId pageId = new PageId(pageTitle, -1);

			// Compile the retrieved page
			Compiler compiler = new Compiler(config);
			cp = compiler.postprocess(pageId, this.getText(), null);
		}catch(Exception e){
			throw new WikiApiException(e);
		}
		return cp;
	}


	///////////////////////////////////////////////////////////////////
	/*
	 * The methods getInlinkAnchors() and getOutLinkAnchors() have not yet been
	 * migrated to the SWEBLE parser. The original versions based on the
	 * JWPL MediaWikiParser can be found in
	 * de.tudarmstadt.ukp.wikipedia.parser.LinkAnchorExtractor
	 */
	///////////////////////////////////////////////////////////////////nchorText = l.getText();
				if (!anchorText.equals(targetTitle)) {
					Set<String> anchors;
					if (outAnchors.containsKey(targetTitle)) {
						anchors = outAnchors.get(targetTitle);
					}
					else {
						anchors = new HashSet<String>();
					}
					anchors.add(anchorText);
					outAnchors.put(targetTitle, anchors);
				}
			}
		}
		return outAnchors;
	}


	/**
	 * Returns a string with infos about this page object.
	 *
	 * @return A string with infos about this page object.
	 * @throws WikiApiException
	 */
	protected String getPageInfo()
		throws WikiApiException
	{
		StringBuffer sb = new StringBuffer(1000);

		sb.append("ID             : " + __getId() + LF);
		sb.append("PageID         : " + getPageId() + LF);
		sb.append("Name           : " + getTitle() + LF);
		sb.append("Disambiguation : " + isDisambiguation() + LF);
		sb.append("Redirects" + LF);
		for (String redirect : getRedirects()) {
			sb.append("  " + redirect + LF);
		}
		sb.append("Categories" + LF);