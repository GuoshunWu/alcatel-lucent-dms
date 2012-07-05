
    alter table dms.ALCATEL_LANGUAGE_CODE 
        drop 
        foreign key FK18E92E87E5130A46

    alter table dms.APPLICATION 
        drop 
        foreign key FKDCF7993013205EA7

    alter table dms.CHARSET 
        drop 
        foreign key FK56D8ED2CE5130A46

    alter table dms.DICTIONARY 
        drop 
        foreign key FK378F5C96F32D7CAE

    alter table dms.DICTIONARY_LANGUAGE 
        drop 
        foreign key FK34DE20E17892C52E

    alter table dms.DICTIONARY_LANGUAGE 
        drop 
        foreign key FK34DE20E14A66686

    alter table dms.DICTIONARY_LANGUAGE 
        drop 
        foreign key FK34DE20E1E5130A46

    alter table dms.LABEL 
        drop 
        foreign key FK44D86D42C7F464E

    alter table dms.LABEL 
        drop 
        foreign key FK44D86D4D2ABE426

    alter table dms.LABEL 
        drop 
        foreign key FK44D86D44A66686

    alter table dms.PRODUCT_VERSION 
        drop 
        foreign key FKE52769483980FE4E

    alter table dms.TEXT 
        drop 
        foreign key FK273D2D54E4D252

    alter table dms.TRANSLATION 
        drop 
        foreign key FK129D8691D2ABE426

    alter table dms.TRANSLATION 
        drop 
        foreign key FK129D8691E5130A46

    drop table if exists dms.ALCATEL_LANGUAGE_CODE

    drop table if exists dms.APPLICATION

    drop table if exists dms.CHARSET

    drop table if exists dms.CONTEXT

    drop table if exists dms.DICTIONARY

    drop table if exists dms.DICTIONARY_LANGUAGE

    drop table if exists dms.LABEL

    drop table if exists dms.LANGUAGE

    drop table if exists dms.PRODUCT

    drop table if exists dms.PRODUCT_VERSION

    drop table if exists dms.TEXT

    drop table if exists dms.TRANSLATION

    create table dms.ALCATEL_LANGUAGE_CODE (
        CODE varchar(255) not null,
        DEFAULT_CODE bit,
        LANGUAGE_ID bigint not null unique,
        primary key (CODE)
    ) type=InnoDB

    create table dms.APPLICATION (
        ID bigint not null auto_increment,
        NAME varchar(255),
        PRODUCT_VERSION_ID bigint not null,
        primary key (ID)
    ) type=InnoDB

    create table dms.CHARSET (
        ID bigint not null auto_increment,
        NAME varchar(255),
        NO integer,
        LANGUAGE_ID bigint,
        primary key (ID)
    ) type=InnoDB

    create table dms.CONTEXT (
        ID bigint not null auto_increment,
        NAME varchar(255),
        primary key (ID)
    ) type=InnoDB

    create table dms.DICTIONARY (
        ID bigint not null auto_increment,
        NAME varchar(255) not null unique,
        FORMAT varchar(20),
        ENCODING varchar(20),
        PATH varchar(255),
        APPLICATION_ID bigint not null,
        LOCKED bit,
        primary key (ID)
    ) type=InnoDB

    create table dms.DICTIONARY_LANGUAGE (
        ID bigint not null auto_increment,
        DICTIONARY_ID bigint,
        LANGUAGE_CODE varchar(255),
        LANGUAGE_ID bigint not null unique,
        CHARSET_ID bigint not null unique,
        primary key (ID)
    ) type=InnoDB

    create table dms.LABEL (
        ID bigint not null auto_increment,
        DICTIONARY_ID bigint,
        REFERENCE varchar(255),
        LABEL_KEY varchar(255),
        DESCRIPTION varchar(255),
        MAX_LENGTH varchar(255),
        CONTEXT_ID bigint not null unique,
        TEXT_ID bigint not null unique,
        primary key (ID)
    ) type=InnoDB

    create table dms.LANGUAGE (
        ID bigint not null auto_increment,
        NAME varchar(255),
        ISO_CODE varchar(255),
        primary key (ID)
    ) type=InnoDB

    create table dms.PRODUCT (
        ID bigint not null auto_increment,
        NAME varchar(255) not null,
        primary key (ID)
    ) type=InnoDB

    create table dms.PRODUCT_VERSION (
        ID bigint not null auto_increment,
        NAME varchar(255) not null,
        PRODUCT_ID bigint not null,
        primary key (ID)
    ) type=InnoDB

    create table dms.TEXT (
        ID bigint not null auto_increment,
        REFERENCE varchar(255),
        STATUS integer,
        context bigint not null unique,
        primary key (ID)
    ) type=InnoDB

    create table dms.TRANSLATION (
        ID bigint not null auto_increment,
        TRANSLATION varchar(255),
        TEXT_ID bigint not null unique,
        LANGUAGE_ID bigint not null unique,
        primary key (ID)
    ) type=InnoDB

    alter table dms.ALCATEL_LANGUAGE_CODE 
        add index FK18E92E87E5130A46 (LANGUAGE_ID), 
        add constraint FK18E92E87E5130A46 
        foreign key (LANGUAGE_ID) 
        references dms.LANGUAGE (ID)

    alter table dms.APPLICATION 
        add index FKDCF7993013205EA7 (PRODUCT_VERSION_ID), 
        add constraint FKDCF7993013205EA7 
        foreign key (PRODUCT_VERSION_ID) 
        references dms.PRODUCT_VERSION (ID)

    alter table dms.CHARSET 
        add index FK56D8ED2CE5130A46 (LANGUAGE_ID), 
        add constraint FK56D8ED2CE5130A46 
        foreign key (LANGUAGE_ID) 
        references dms.LANGUAGE (ID)

    alter table dms.DICTIONARY 
        add index FK378F5C96F32D7CAE (APPLICATION_ID), 
        add constraint FK378F5C96F32D7CAE 
        foreign key (APPLICATION_ID) 
        references dms.APPLICATION (ID)

    alter table dms.DICTIONARY_LANGUAGE 
        add index FK34DE20E17892C52E (CHARSET_ID), 
        add constraint FK34DE20E17892C52E 
        foreign key (CHARSET_ID) 
        references dms.CHARSET (ID)

    alter table dms.DICTIONARY_LANGUAGE 
        add index FK34DE20E14A66686 (DICTIONARY_ID), 
        add constraint FK34DE20E14A66686 
        foreign key (DICTIONARY_ID) 
        references dms.DICTIONARY (ID)

    alter table dms.DICTIONARY_LANGUAGE 
        add index FK34DE20E1E5130A46 (LANGUAGE_ID), 
        add constraint FK34DE20E1E5130A46 
        foreign key (LANGUAGE_ID) 
        references dms.LANGUAGE (ID)

    alter table dms.LABEL 
        add index FK44D86D42C7F464E (CONTEXT_ID), 
        add constraint FK44D86D42C7F464E 
        foreign key (CONTEXT_ID) 
        references dms.CONTEXT (ID)

    alter table dms.LABEL 
        add index FK44D86D4D2ABE426 (TEXT_ID), 
        add constraint FK44D86D4D2ABE426 
        foreign key (TEXT_ID) 
        references dms.TEXT (ID)

    alter table dms.LABEL 
        add index FK44D86D44A66686 (DICTIONARY_ID), 
        add constraint FK44D86D44A66686 
        foreign key (DICTIONARY_ID) 
        references dms.DICTIONARY (ID)

    alter table dms.PRODUCT_VERSION 
        add index FKE52769483980FE4E (PRODUCT_ID), 
        add constraint FKE52769483980FE4E 
        foreign key (PRODUCT_ID) 
        references dms.PRODUCT (ID)

    alter table dms.TEXT 
        add index FK273D2D54E4D252 (context), 
        add constraint FK273D2D54E4D252 
        foreign key (context) 
        references dms.CONTEXT (ID)

    alter table dms.TRANSLATION 
        add index FK129D8691D2ABE426 (TEXT_ID), 
        add constraint FK129D8691D2ABE426 
        foreign key (TEXT_ID) 
        references dms.TEXT (ID)

    alter table dms.TRANSLATION 
        add index FK129D8691E5130A46 (LANGUAGE_ID), 
        add constraint FK129D8691E5130A46 
        foreign key (LANGUAGE_ID) 
        references dms.LANGUAGE (ID)
