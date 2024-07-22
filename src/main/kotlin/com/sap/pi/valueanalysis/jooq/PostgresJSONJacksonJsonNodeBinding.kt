package com.sap.pi.valueanalysis.jooq

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import org.jooq.*
import org.jooq.impl.DSL
import java.io.IOException
import java.sql.SQLFeatureNotSupportedException
import java.sql.Types
import java.util.*

class PostgresJSONJacksonJsonNodeBinding : Binding<JSONB, JsonNode> {
    override fun converter(): Converter<JSONB, JsonNode> {
        return PostgresJSONJacksonJsonNodeConverter()
    }

    override fun sql(ctx: BindingSQLContext<JsonNode>?) {
        // This ::json cast is explicitly needed by PostgreSQL:
        ctx!!.render().visit(DSL.`val`(ctx.convert(converter()).value())).sql("::json")
    }

    override fun register(ctx: BindingRegisterContext<JsonNode>?) {
        ctx!!.statement().registerOutParameter(ctx.index(), Types.VARCHAR)
    }

    override fun set(ctx: BindingSetStatementContext<JsonNode>?) {
        ctx!!.statement().setString(
            ctx.index(),
            Objects.toString(ctx.convert(converter()).value(), null),
        )
    }

    override fun set(ctx: BindingSetSQLOutputContext<JsonNode>?) {
        throw SQLFeatureNotSupportedException()
    }

    override fun get(ctx: BindingGetResultSetContext<JsonNode>?) {
        ctx!!.convert(converter()).value(JSONB.valueOf(ctx.resultSet().getString(ctx.index())))
    }

    override fun get(ctx: BindingGetStatementContext<JsonNode>?) {
        ctx!!.convert(converter()).value(JSONB.valueOf(ctx.statement().getString(ctx.index())))
    }

    override fun get(ctx: BindingGetSQLInputContext<JsonNode>?) {
        throw SQLFeatureNotSupportedException()
    }
}

class PostgresJSONJacksonJsonNodeConverter : Converter<JSONB, JsonNode> {
    override fun from(databaseObject: JSONB?): JsonNode {
        return try {
            if (databaseObject == null) NullNode.instance else ObjectMapper().readTree(databaseObject.toString() + "")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun to(userObject: JsonNode?): JSONB? {
        return try {
            if (userObject == null || userObject == NullNode.instance) {
                null
            } else {
                JSONB.valueOf(
                    ObjectMapper().writeValueAsString(
                        userObject,
                    ),
                )
            }
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
    }

    override fun fromType(): Class<JSONB> {
        return JSONB::class.java
    }

    override fun toType(): Class<JsonNode> {
        return JsonNode::class.java
    }
}
