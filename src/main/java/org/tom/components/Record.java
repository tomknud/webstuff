package org.tom.components;

public class Record {

    private String text;

    private String link;

    public String getText() {

      return text;

    }

    public String getLink() {

      return link;

    }

 

    public Record(String text, String link) {

      super();

      this.text = text;

      this.link = link;

    }

 

    @Override

    public String toString() {

      return "Link=" + link + " Text=" + text + " Link Hash = " + link.hashCode();

    }

   

    @Override

    public boolean equals(Object o) {

      if(o instanceof Record) {

        if(link.equals(((Record) o).link)) {

          return true;

        }

      }

      return false;

    }

   

    @Override

    public int hashCode() {

      return link.hashCode();

    }

 

  }