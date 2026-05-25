package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.Book
import com.example.data.model.Annotation
import com.example.data.model.LiteraryConnection
import com.example.data.model.ReadingJournal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class LiteratureRepository(
    private val bookDao: BookDao,
    private val annotationDao: AnnotationDao,
    private val connectionDao: LiteraryConnectionDao,
    private val journalDao: ReadingJournalDao
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allAnnotations: Flow<List<Annotation>> = annotationDao.getAllAnnotations()
    val allConnections: Flow<List<LiteraryConnection>> = connectionDao.getAllConnections()
    val allJournals: Flow<List<ReadingJournal>> = journalDao.getAllJournals()

    fun getBookById(id: String): Flow<Book?> = bookDao.getBookById(id)
    suspend fun getBookByIdSync(id: String): Book? = bookDao.getBookByIdSync(id)

    fun getAnnotationsForBook(bookId: String): Flow<List<Annotation>> =
        annotationDao.getAnnotationsForBook(bookId)

    fun getJournalById(id: String): Flow<ReadingJournal?> = journalDao.getJournalById(id)

    suspend fun insertBook(book: Book) = bookDao.insertBook(book)
    suspend fun updateBook(book: Book) = bookDao.updateBook(book)
    suspend fun updateBookProgress(bookId: String, progress: Float, currentPage: Int) =
        bookDao.updateReadingProgress(bookId, progress, currentPage)

    suspend fun deleteBook(id: String) = bookDao.deleteBookById(id)

    suspend fun insertAnnotation(annotation: Annotation) = annotationDao.insertAnnotation(annotation)
    suspend fun deleteAnnotation(id: String) = annotationDao.deleteAnnotationById(id)

    suspend fun insertConnection(connection: LiteraryConnection) =
        connectionDao.insertConnection(connection)
    suspend fun deleteConnection(id: String) = connectionDao.deleteConnectionById(id)

    suspend fun insertJournal(journal: ReadingJournal) = journalDao.insertJournal(journal)
    suspend fun deleteJournal(id: String) = journalDao.deleteJournalById(id)

    // Prepopulate database with realistic classics, essays & commentary
    suspend fun prepopulateDefaultDatabase() {
        val count = allBooks.firstOrNull()?.size ?: 0
        if (count == 0) {
            // Prepopulate Books
            val meditationsText = """
                Book I.
                1. From my grandfather Verus I learned good morals and the government of my temper.
                2. From the reputation and memory of my father, modesty and a manly character.
                3. From my mother, piety and beneficence, and abstinence, not only from evil deeds, but even from evil thoughts; and further, simplicity in my way of living, far removed from the habits of the rich.
                4. From my great-grandfather, not to have frequented public schools, and to have had good teachers at home, and to know that on such things a man should spend liberally.
                
                Book II.
                1. Begin the morning by saying to thyself, I shall meet with the busybody, the ungrateful, arrogant, deceitful, envious, unsocial. All these things happen to them by reason of their ignorance of what is good and evil. But I who have seen the nature of the good that it is beautiful, and of the bad that it is ugly, and of the mutineer himself, that his nature is akin to my own, can neither be injured by any of them, for no one can fix on me what is ugly, nor can I be angry with my kinsman, nor hate him. We are made for cooperation, like feet, like hands, like the rows of the upper and lower teeth. To act against one another then is contrary to nature; and it is acting against one another to be vexed and to turn away.
                2. Whatever this is that I am, it is a little flesh and breath, and the ruling part. Throw away thy books; no longer distract thyself; it is not allowed. But as if thou wast now dying, despise the flesh; it is nothing but blood and bones and a network, a contexture of nerves, veins, and arteries.
                
                Book IV.
                49. Be like the promontory against which the waves continually break, but it stands firm and tames the fury of the water around it. 
                "Unhappy am I, because this has happened to me."—Not so, but "Happy am I, though this has happened to me, because I continue free from pain, neither crushed by the present nor fearing the future." For such an event as this might have happened to any man, but not every man would have continued free from pain on such an occasion. 
                Remember too on every occasion which leads thee to vexation to apply this principle: not that this is a misfortune, but that to bear it nobly is good fortune.
            """.trimIndent()

            val senecaText = """
                Chapter I.
                The majority of mortals, Paulinus, complain bitterly of the spitefulness of Nature, because we are born for a brief span of life, because even this space that has been granted to us rushes by so speedily and so swiftly that all save a very few find life at an end just when they are getting ready to live. 
                It is not that we have a short time to live, but that we waste much of it. Life is long enough, and a sufficiently generous estimate has been given to us for the highest achievements if it were all well invested. But when it is squandered in luxury and carelessness, when it is devoted to no good end, forced at last by the ultimate necessity we perceive that it has passed away before we were aware that it was passing. 
                So it is—we are not given a short life but we make it short, and we are not ill-supplied but wasteful of it. Just as great and princely wealth is scattered in a moment when it comes into the hands of a bad owner, while wealth however limited, if it is entrusted to a good guardian, increases by use, so our life is amply sufficient for him who orders it well.
                
                Chapter III.
                Why do you delay? Why do you stand idle? Unless you seize the day, it flees. Even if you seize it, it will still flee; therefore you must compete with time's swiftness in the speed of using it, and you must drink quickly as from a rapid torrent that will not always flow. 
                No one will bring back your years, no one will restore you to yourself. Life will follow the path it began and will neither reverse nor check its course. It will make no noise, it will remind you of its speed. It will slip away in silence.
            """.trimIndent()

            val leviathanText = """
                Chapter XIII: Of the Natural Condition of Mankind as Concerning Their Felicity and Misery.
                
                Nature hath made men so equal in the faculties of body and mind, as that, though there be found one man sometimes manifestly stronger in body or of quicker mind than another, yet when all is reckoned together the difference between man and man is not so considerable as that one man can thereupon claim to himself any benefit to which another may not pretend as well as he.
                
                From this equality of ability ariseth equality of hope in the attaining of our ends. And therefore if any two men desire the same thing, which nevertheless they cannot both enjoy, they become enemies; and in the way to their end (which is principally their own conservation, and sometimes their delectation only) endeavour to destroy or subdue one another.
                
                Hereby it is manifest that during the time men live without a common power to keep them all in awe, they are in that condition which is called war; and such a war as is of every man against every man. 
                In such condition there is no place for industry, because the fruit thereof is uncertain: and consequently no culture of the earth; no navigation, nor use of the commodities that may be imported by sea; no commodious building; no instruments of moving and removing such things as require much force; no knowledge of the face of the earth; no account of time; no arts; no letters; no society; and which is worst of all, continual fear, and danger of violent death; and the life of man, solitary, poor, nasty, brutish, and short.
            """.trimIndent()

            val virginiaWoolfText = """
                Chapter I.
                But, you may say, we asked you to speak about women and fiction—what has that got to do with a room of one's own? I will try to explain. 
                When you ask me to speak of women and fiction I should sit down on the banks of a river and think what those words mean. They might mean simply a few remarks about Fanny Burney; a few more about Jane Austen; a tribute to the Brontës and a sketch of Haworth Parsonage under the snow; some witticisms if possible about Miss Mitford; a respectful allusion to George Eliot; a reference to Mrs. Gaskell and one would have done. 
                But at second sight the words seemed not so simple. The title women and fiction might mean, and you may have meant it to mean, women and what they are like; or it might mean women and the fiction that they write; or it might mean women and the theories that have been written about them; or it might mean that somehow these three are inextricably mixed together and you want me to consider them in that light. 
                
                But when I began to consider the subject in this last way, which seemed the most interesting, I soon saw that it had one fatal drawback. I should never be able to come to a conclusion. I should never be able to fulfil what is, I understand, the first duty of a lecturer—to hand you after an hour's discourse a nugget of pure truth to wrap up between the pages of your notebooks and keep on the mantelpiece for ever. 
                All I could do was to offer you an opinion upon one minor point—a woman must have money and a room of her own if she is to write fiction; and that, as you will see, leaves the great problem of the true nature of woman and the true nature of fiction unsolved.
            """.trimIndent()

            val odysseyText = """
                Book I.
                Sing in me, Muse, and through me tell the story
                of that man skilled in all ways of contending,
                the wanderer, harried for years on end,
                after he plundered the stronghold on the proud height of Troy.
                
                He saw the townlands and learned the minds of many distant men,
                and weathered many bitter nights and days in his deep heart at sea,
                while he fought only to save his life, to bring his shipmates home.
                But not by will nor valor could he save them,
                for their own recklessness destroyed them all—
                children and fools, they shared and devoured the cattle of Lord Helios,
                the Sun, and he who moves all day in heaven
                took from their eyes the dawn of their return.
                
                Of these things, speak, Goddess, daughter of Zeus,
                beginning at whatever point you will, and tell our people now.
            """.trimIndent()

            val book1Id = "meditations"
            val book2Id = "seneca_shortness"
            val book3Id = "leviathan"
            val book4Id = "room_own"
            val book5Id = "odyssey"

            insertBook(Book(
                id = book1Id,
                title = "Meditations",
                author = "Marcus Aurelius",
                category = "Philosophy",
                content = meditationsText,
                progress = 0.3f,
                currentPage = 2,
                totalPages = 12,
                year = "180 AD",
                description = "A series of personal writings by Marcus Aurelius, Roman Emperor, recording his private notes to himself and ideas on Stoic philosophy."
            ))

            insertBook(Book(
                id = book2Id,
                title = "On the Shortness of Life",
                author = "Seneca",
                category = "Philosophy",
                content = senecaText,
                progress = 0.1f,
                currentPage = 1,
                totalPages = 8,
                year = "49 AD",
                description = "A moral essay written by Seneca the Younger, a Stoic philosopher, offering urgent counsel on the value of time and self-possession."
            ))

            insertBook(Book(
                id = book3Id,
                title = "Leviathan",
                author = "Thomas Hobbes",
                category = "Research",
                content = leviathanText,
                progress = 0.0f,
                currentPage = 1,
                totalPages = 24,
                year = "1651 AD",
                description = "Hobbes' masterpiece on the structure of society and legitimate government, formulating one of the earliest social contract theories."
            ))

            insertBook(Book(
                id = book4Id,
                title = "A Room of One's Own",
                author = "Virginia Woolf",
                category = "Fiction",
                content = virginiaWoolfText,
                progress = 0.5f,
                currentPage = 3,
                totalPages = 6,
                year = "1929 AD",
                description = "An extended essay based on a series of lectures delivered by Virginia Woolf, advocating for physical and financial independence for creative women."
            ))

            insertBook(Book(
                id = book5Id,
                title = "The Odyssey",
                author = "Homer",
                category = "Classics",
                content = odysseyText,
                progress = 0.0f,
                currentPage = 1,
                totalPages = 24,
                year = "8th Century BC",
                description = "One of two major ancient Greek epic poems attributed to Homer, chronicling Odysseus' epic ten-year journey home to Ithaca."
            ))

            // Prepopulate some intellectually rich default Annotations
            insertAnnotation(Annotation(
                id = "ann1",
                bookId = book1Id,
                excerpt = "The impediment to action advances action. What stands in the way becomes the way.",
                note = "This is the classic formulation of Stoic transmuting. Obstacles are not interruptions; they are the material of work.",
                commentType = "Philosophical Commentary",
                tags = "Stoicism,Action,Growth",
                offsetStart = 450,
                offsetEnd = 540
            ))

            insertAnnotation(Annotation(
                id = "ann2",
                bookId = book2Id,
                excerpt = "It is not that we have a short time to live, but that we waste much of it.",
                note = "Seneca emphasizes that human lifespan is sufficient for high pursuits, provided we have the intellectual discipline to resist trivial distractions.",
                commentType = "Philosophical Commentary",
                tags = "Time,Discipline,Seneca",
                offsetStart = 50,
                offsetEnd = 130
            ))

            insertAnnotation(Annotation(
                id = "ann3",
                bookId = book3Id,
                excerpt = "solitary, poor, nasty, brutish, and short.",
                note = "Hobbes describes the state of nature absent a sovereign power. The absolute baseline of human vulnerability when institutional order ruptures.",
                commentType = "Historical Note",
                tags = "Sovereignty,Empire,State of Nature",
                offsetStart = 800,
                offsetEnd = 845
            ))

            // Prepopulate some beautiful connections inside the graph
            insertConnection(LiteraryConnection(
                id = "conn1",
                sourceBookId = book1Id,
                targetBookId = book2Id,
                sourceConcept = "Stoicism",
                targetConcept = "Finitude & Time",
                description = "Both authors view the human mind as the ultimate sovereign territory, though Aurelius focuses on internal duties of a ruler, while Seneca stresses reclaiming time from social obligations."
            ))

            insertConnection(LiteraryConnection(
                id = "conn2",
                sourceBookId = book1Id,
                targetBookId = book3Id,
                sourceConcept = "Cosmopolis",
                targetConcept = "Leviathan Sovereign",
                description = "Aurelius looks at cooperation and natural law of the citizenship of the world, whereas Hobbes sees men in conflict requiring a structured artificial sovereign to prevent brutal annihilation."
            ))

            // Prepopulate a gorgeous journal entry
            insertJournal(ReadingJournal(
                id = "journ1",
                bookId = book1Id,
                title = "Reflections on Internal Sovereignty",
                body = "Reading Meditations early this morning. It strikes me how heavily Aurelius relies on spatial visual metaphors—the promontory standing firm against the sea. It fits my idea of the reading workspace as an intellectual armor against immediate digital reactions. I should connect this to Seneca's letter on reclaiming the self from trivialities.",
                timestamp = System.currentTimeMillis() - 86400000 // Yesterday
            ))
        }
    }
}
