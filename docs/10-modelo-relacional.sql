-- =====================================================================
-- Control Prendario - Modelo relacional (DDL)
-- Motor objetivo: MySQL 8 / TiDB Cloud (compatible MySQL)
--
-- Script generado con Hibernate ORM 6.5.3 (jakarta.persistence
-- schema-generation, acción "create") a partir de las entidades JPA del
-- paquete control.prendario.model, sin conexión a base de datos.
-- Rama feature/yohan. Generación asistida por Claude Code (ver docs/13-uso-ia.md).
--
-- Tablas: clientes, vehiculos, prestamos, prestamos_maquinas,
--         pagos, pagos_maquinas, movimientos, auditoria
-- =====================================================================

CREATE DATABASE IF NOT EXISTS controlprendario
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE controlprendario;


create table auditoria (
    fecha_evento datetime(6) not null,
    id bigint not null auto_increment,
    recurso_id bigint,
    resultado varchar(20) not null,
    recurso varchar(100) not null,
    usuario varchar(100) not null,
    accion varchar(150) not null,
    primary key (id)
) engine=InnoDB;

create table clientes (
    activo bit,
    fecha_nacimiento date,
    fecha_registro datetime(6),
    id_cliente bigint not null auto_increment,
    apellidos varchar(255),
    ciudad varchar(255),
    direccion varchar(255),
    email varchar(255),
    nombres varchar(255),
    numero_documento varchar(255),
    telefono varchar(255),
    tipo_documento enum ('CEDULA','DNI','PASAPORTE'),
    primary key (id_cliente)
) engine=InnoDB;

create table movimientos (
    monto decimal(12,2) not null,
    fecha_movimiento datetime(6),
    id_movimiento bigint not null auto_increment,
    tipo_movimiento enum ('ENTRADA','SALIDA') not null,
    primary key (id_movimiento)
) engine=InnoDB;

create table pagos (
    es_descuento bit,
    monto_pagado decimal(12,2) not null,
    corregido_por_pago_id bigint,
    fecha_correccion datetime(6),
    fecha_pago datetime(6),
    id_pago bigint not null auto_increment,
    id_prestamo bigint not null,
    reemplaza_pago_id bigint,
    motivo_correccion varchar(1000),
    observaciones TEXT,
    estado_pago enum ('ACTIVO','CORREGIDO'),
    metodo_pago enum ('EFECTIVO','TARJETA','TRANSFERENCIA'),
    tipo_pago enum ('CAPITAL','INTERES','MORA'),
    primary key (id_pago)
) engine=InnoDB;

create table pagos_maquinas (
    es_descuento bit,
    monto_pagado decimal(12,2) not null,
    corregido_por_pago_id bigint,
    fecha_correccion datetime(6),
    fecha_pago datetime(6),
    id_pago_maquina bigint not null auto_increment,
    id_prestamo_maquina bigint not null,
    reemplaza_pago_id bigint,
    motivo_correccion varchar(1000),
    observaciones TEXT,
    estado_pago enum ('ACTIVO','CORREGIDO'),
    metodo_pago enum ('EFECTIVO','TARJETA','TRANSFERENCIA'),
    tipo_pago enum ('CAPITAL','INTERES','MORA'),
    primary key (id_pago_maquina)
) engine=InnoDB;

create table prestamos (
    activo bit,
    capital_pagado decimal(38,2),
    capital_pendiente decimal(38,2),
    interes_pagado decimal(38,2),
    interes_pendiente decimal(38,2),
    interes_total decimal(38,2),
    monto_prestamo decimal(38,2),
    tasa_interes decimal(38,2),
    fecha_prestamo datetime(6),
    fecha_vencimiento datetime(6),
    id_cliente bigint,
    id_prestamo bigint not null auto_increment,
    id_vehiculo bigint,
    observaciones varchar(255),
    estado_prestamo enum ('ACTIVO','PAGADO','PENDIENTE','VENCIDO'),
    primary key (id_prestamo)
) engine=InnoDB;

create table prestamos_maquinas (
    activo bit,
    capital_pagado decimal(38,2),
    capital_pendiente decimal(38,2),
    fecha_vencimiento date,
    interes_pagado decimal(38,2),
    interes_pendiente decimal(38,2),
    interes_total decimal(38,2),
    monto_prestamo decimal(38,2),
    tasa_interes decimal(38,2),
    fecha_prestamo datetime(6),
    id_cliente bigint,
    id_prestamo_maquina bigint not null auto_increment,
    marca varchar(255),
    observaciones varchar(255),
    tipo_maquina varchar(255),
    estado_prestamo enum ('ACTIVO','PAGADO','PENDIENTE','VENCIDO'),
    primary key (id_prestamo_maquina)
) engine=InnoDB;

create table vehiculos (
    activo bit,
    cilindraje integer,
    fecha_matricula date,
    modelo integer,
    fecha_registro datetime(6),
    id_vehiculo bigint not null auto_increment,
    color varchar(255),
    linea varchar(255),
    marca varchar(255),
    numero_chasis varchar(255),
    numero_documento_propietario varchar(255),
    numero_motor varchar(255),
    placa varchar(255),
    propietario varchar(255),
    sitio_matricula varchar(255),
    tipo_vehiculo enum ('AUTOMOVIL','CAMION','CAMIONETA','MOTOCICLETA'),
    primary key (id_vehiculo)
) engine=InnoDB;

alter table clientes 
   add constraint UK2viccgf178bd74vfbq8ctsv8t unique (numero_documento);

alter table vehiculos 
   add constraint UKa8w6omovfa10q8eyjalqas391 unique (placa);

alter table pagos 
   add constraint FK7na2pyprg2mjqfhvynhig11mi 
   foreign key (id_prestamo) 
   references prestamos (id_prestamo);

alter table pagos_maquinas 
   add constraint FK2e0a0f4ajjyrgni5iutbetv8l 
   foreign key (id_prestamo_maquina) 
   references prestamos_maquinas (id_prestamo_maquina);

alter table prestamos 
   add constraint FKr39piepds6x3dlmd1ba0byf2r 
   foreign key (id_cliente) 
   references clientes (id_cliente);

alter table prestamos 
   add constraint FKbktldfne5drl4aofugb9r62pd 
   foreign key (id_vehiculo) 
   references vehiculos (id_vehiculo);

alter table prestamos_maquinas 
   add constraint FK27qihbfh8ysj6pmynl7u9wluw 
   foreign key (id_cliente) 
   references clientes (id_cliente);
