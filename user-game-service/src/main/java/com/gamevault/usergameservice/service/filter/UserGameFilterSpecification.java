package com.gamevault.usergameservice.service.filter;

import com.gamevault.usergameservice.db.model.QGame;
import com.gamevault.usergameservice.db.model.QUserGame;
import com.querydsl.core.types.dsl.BooleanExpression;

@FunctionalInterface
public interface UserGameFilterSpecification {
    BooleanExpression toExpression(QUserGame userGame, QGame game);
}
