package com.uoscs09.theuos.http.parse;

import java.io.IOException;
import java.util.ArrayList;

@Deprecated
public class ParserSubjectList extends OApiParser<ArrayList<String>, ArrayList<String>> {
    private final String[] PTN = {"subject_no", "subject_nm", "class_div"};

    @Override
    public ArrayList<ArrayList<String>> parse(String body) throws IOException {
        return parseToArrayList(body.split(OApiParser.LIST), PTN);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initItem(ArrayList<String> parsedStringList, ArrayList<ArrayList<String>> returningList) {
        returningList.add((ArrayList<String>) parsedStringList.clone());
    }
}
