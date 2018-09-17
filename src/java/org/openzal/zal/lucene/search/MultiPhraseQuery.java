package org.openzal.zal.lucene.search;

import com.sun.istack.NotNull;
import org.openzal.zal.lucene.index.Term;

import java.util.List;

public class MultiPhraseQuery
  extends Query
{

  public MultiPhraseQuery(Term... terms)
  {
    super(new org.apache.lucene.search.MultiPhraseQuery());

    if( terms.length > 0 )
    {
      add(terms);
    }
  }

  @Override
  protected org.apache.lucene.search.MultiPhraseQuery getZimbra()
  {
    return (org.apache.lucene.search.MultiPhraseQuery) super.getZimbra();
  }

  public void add(@NotNull Term term)
  {
    getZimbra().add(term.toZimbra(org.apache.lucene.index.Term.class));
  }

  public void add(@NotNull List<Term> terms)
  {
    add(terms.toArray(new Term[0]));
  }

  public void add(@NotNull Term[] terms)
  {
    org.apache.lucene.index.Term[] zimbraArray = new org.apache.lucene.index.Term[terms.length];

    for( int i = 0; i < zimbraArray.length; i++ )
    {
      zimbraArray[i] = terms[i].toZimbra(org.apache.lucene.index.Term.class);
    }

    getZimbra().add(zimbraArray);
  }

}
