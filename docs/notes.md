Wildfly Subsystem Notes
-----------------------

* Dependency management pomocou v InjectedValue<T> v services
* Definicia zavislosti v ResourceAddHandlery pomocou ServiceBuilderu()
* zdroj inspiracie pre spravu zavislosti [PicketLink2 subsystem](https://github.com/picketlink/picketlink-as-subsystem)


ABRT notes
----------

* ABRT je sada nástrojov pre reportovanie a detekciu problémov (chýb, atypického chovania).
* Umožňuje nájdenie riešenia chýb pomocou založenia ticketov do rôznych prostredí (bugzilla, knowledge base articles, package updater)
* Práca by mala využívať hlavne serverovú časť ABRT-u, ktorá zabezpečuje klasifikáciu a uloženie chyby do prostredia, ktoré umožňuje 
  vývojárom a skúseným uživateľom daného projektu, chybu nájsť a pomôcť s jej riešením.
* ABRT momentálne umožňuje detekciu rôznych druhov chýb (C/C++ bugs, Python stacktrace, chyby v kernely a v XORG)
* V Jave momentálne funguje len ochytávanie chyb pomocou Java Virtual Machine Tools Interface (proof of concept)
* Tieto chyby sú reportované do socketu (/var/run/abrt/abrt.socket), ktorý chyby preposiela na server
* Chyby sú na server zasielané v uReport formáte
* Klient, ktorý zabezpečuje zber chyb musí zabezpečiť anonymitu. Správa nesmie obsahovať žiadne súkromné informácie,
  teda žiadne názvy tried a balíkov, ktoré sú súčastou aplikácie a nemali by byť prístupne verejne.
  Služba, ktorá bude tickety zasielať musí teda podporovať odstranenie častí stacktracu a chybových správ, ktoré obsahujú citlivé informácie
  Služba by mala implementovať filter, ktorý napríklad na základe názvu balíka zistí, či je možné túto časť pridať do zasielaného ticketu.
* Existujúce projekty, ktoré intregrujú Javu a ABRT 
  * JABRT - umožňuje zasielanie ticketov
  * abrt-jboss - implementuje java.util.handler, ktorý odchytáva zalogované správy

uReport (micro report)
======================

* formát založený na JSON
* momentálne tri verzie
  * verzia 0 
    * neverzované reporty
    * deprecated
  * verzia 1
    * každý ticket obsahuje stacktrace a info o systéme (verzia OS, architektúra, verzia jadra)
    * úzko prepojený s Fedorou
    * naročnejšia adaptácia na rôzne typy reportovaných chýb (chýbajúci stacktrace alebo bližšie info o architektúre)
    * všetky typy ticketov sú spracovávane rovnako čo vedie k zníženej kvalite riešenia
    * podporované typy ticketov (python, kerneloops, userspace)
  * verzia 2
    * založená na nástroji FAF (https://github.com/abrt/faf)
    * každý ticket musí obsahovať len spoločné metadata a identifikáciu typu problému
    * podporované typy sú OS - tickety, ktoré vznikli chybou v OS a majú byť spracované nejakým OS pluginom
                          problem - všetky ostatné (aj java)
                                  - deleguje na konkretné pluginy, ktoré spracovávajú podtypy ticketov (python, java)
    * formát - ureport0_version - nusí byť 2
             - problem - data pre spracovanie Problem pluginom
                       - type - typ pluginu, ktorý má problém spracovať
                       - anything else - data špecifická pre daný typ ticketu
             - OS - data pre spracovanie OS pluginom
                  - name - názov OS pluginu, ktorý ma problém spracovať
                  - version - verzia uloženia
                  - arch - architektúra CPU
             - packages - nutné pre OS pluginy
                        - popisujú baličky, ktoré boli ovplyvnené chybou
             - reason - krátka správa popisujúca vzniknutú chybu
             - reporter - identifikácia klienta, ktorý zaslal chybu
                        - name - názov klienta
                        - version - verzia klienta
* uReport attachment 
  * umožňujú priradenie dat k existujúcemu ticketu v bugzille
  * formát - type - typ prílohy (len "RHBZ")
           - bthash - identifikátor bugzilla ticketu
           - data - data, ktoré sa majú pridať k ticketu

Java uReport formát
===================

* rozširuje Problem plugin formát
* obsahuje nasledujúce vlastnosti, ktoré musia byť vyplnené:
  * reason - krátky popis chyby
  * pid - identifikátor procesu
  * typ - stále "java"
  * analyzátor - "java"
  * executable - absolutná cesta k spustenej aplikácii
  * backtrace - obsah stacktracu
* zasielanie ticketov cez HTTP PUT

JABRT
-----

* podporuje všetky vlastnosti Java uReport formátu
* HTTP Rest API
* všetky tickety zasiela do socketu. 
  Low-level riešenie. 
  Pravdepodobne by bolo vhodné využiť knižnicu, ktorá zabezpečuje jednoduchšie zasielanie a spracovanie cez HTTP a parsovanie JSON dokumentov.
* Pre parsovanie JSON-u by som navrhoval Jackson
* Pre HTTP Resteasy (súčasť Wildfly?)
* Endpoint pre zasielanie ticketov by mohol byť definovaný v rámci konfigurácie subsystému.

ABRT-JBoss
----------

* java.util.logging.Handler, ktorý umožňuje zasielanie zalogovaných výnimiek do ABRT-u
* momentálne odchytáva všetky výnimky bez ohľadu na úroveň zanorenia.
* využíva JABRT pre odoslanie výnimiek
* zasielanie správ funguje synchrónne, čo môže spôsobiť spomalenie
* bolo by dobré (žiadúce) umožniť zasielanie správ asynchronne. To môžeme docieliť rozšíreným org.jboss.logging.AsyncHandleru, ktorý ukladá
  LogRecords do BlockingQueue, ktorá je asynchronne spracovávaná zaregistrovanými handlermi
* momentálne je nutná manuálna registrácia handleru v konfiguračnom súbore AS
* subsystém by teda mohol rozšíriť AsyncHandler a ukladať do svojej fronty len správy, ktoré obsahujú výnimku
* alternatívne by bolo možné pri štarte subsystému pre reporting výnimiek zaregistrovať AbrtHandler do existujúcej instancie AsyncHandleru,
  ktorá je inicializovaná pri štarte jboss-as-logging subsystému


Postup riešenia
---------------
1. Implementacia / rozsirenie JABRT loggeru tak, aby podporoval asynchronne odosielanie ticketov do ABRT-u
2. Nova implementacia JABRTU by mala vyuzivat, implementaciu noveho rozhrania ExceptionProcessoru, ktory pripravi vynimky pre odoslanie. Overi, ci sa ma vynimka odoslat a upravi velkost stacktrace, tak aby neobsahoval ziadne sukromne informacie o aplikacii.
3. Implementacia novej sluzby v ramci subsystemu, ktora sa pripoji na VM pomocou Debugging API a bude odchytavat vynimky, v ramci beziaceho virtualneho stroja.
4. Tato sluzba by mala taktiez vyuzivat ExceptionProcessor.

5. Po uspesnej implementacii odosielania a filtrovania vynimiek, bude naimplementovane ukladanie odoslanych zaznamov do lokalnej databazy.
6. Databaza bude ukladat chybovu spravu, odoslani stacktrace, datum prveho vyskytu, url, ktore ziskame z ABRTu po uspesnom ulozeni ticketu na ABRT servery a pocet vyskytov tejto vynimky ak sa uz vyskytla.
7. Ukladanie do databazy by malo vyuzivat JPA sluzbu poskytovanu aplikacnym serverom Wildfly.

8. Poslednym krokom riesenia je naimplementovanie webovej aplikacie, ktora umozni zobrazenie ulozenych vynimiek.
   Ak by ABRT tym dokazal poskytnut aj rozhranie pre stahovanie rieseni jednotlivych ticketov, tak by aplikacia raz za den (24 hodin) skusila ziskat
   riesenia ticketov.

Debugger API
------------

Wildfly musi sam inicializovat debugger pomocou VirtualMachineManageru a pripojit sa sam na seba pomocou AttachingConnectoru.

* Wildfly musi byt spusteni s nasledujucimi parametrami VM "agentlib:jdwp=transport=dt_socket,server=y,address=<port>"
* VM nastartuje debugger.
* Po spusteni debugger vyberie AttachingConnector z VirtualManager.attachingConnectors(), ktoreho nazov zodpoveda parametru transport
* Debugger vyuzije defaultArguments() na nastavenie pripojenia a zmeni adresu pripojenia na adresu, ktoru mu poskytne VM.
* Debugger zavola attach(arguments) pre pripojenie k VM na ktorej bezi Wildfly



Misc
----

GitHub Repository: https://github.com/iref/wildfly-exceptions
