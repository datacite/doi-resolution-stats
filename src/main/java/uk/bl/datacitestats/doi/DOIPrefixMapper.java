package uk.bl.datacitestats.doi;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Not ideal but reasonable first pass implementation of name->prefix mappings
 * provider
 * 
 * TODO: for datacite we can get http://search.datacite.org/list/datacentres for
 * list of names get http://search.datacite.org/list/prefixes for a list of
 * prefixes get
 * http://search.datacite.org/list/prefixes?fq=datacentre_symbol:TIB
 * .GFZ&facet.mincount=1 to match them up.
 * 
 * Or parse whole dump from: curl
 * "http://search.datacite.org/api?q=prefix:*&fl=prefix,datacentre&wt=csv&csv.header=false&rows=99999999"
 * which is datacite-all.json TODO: for crossref we can parse
 * http://www.crossref.org/xref/xml/mddb.xml
 * 
 * @author tom
 * 
 */
@Singleton
public class DOIPrefixMapper {

	// name -> doi list
	private final ImmutableMultimap<String, String> datacentreMap;

	// TODO: inject location?
	@Inject
	public DOIPrefixMapper() {
		datacentreMap = loadBasicDatacentreMap("datacentre-prefixes.json");
	}

	private ImmutableMultimap<String, String> loadBasicDatacentreMap(String file) {
		Multimap<String, String> m = LinkedHashMultimap.create();
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<DatacentrePrefixMapping> prefixes = mapper.readValue(getClass().getResourceAsStream(file),
					new TypeReference<List<DatacentrePrefixMapping>>() {
					});
			for (DatacentrePrefixMapping mapping : prefixes) {
				m.putAll(mapping.datacentre, mapping.prefixes);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ImmutableMultimap.copyOf(m);
	}

	public ImmutableMultimap<String, String> getDatacentreMap() {
		return datacentreMap;
	}

	public static class DatacentrePrefixMapping {
		public String datacentre;
		public List<String> prefixes;
	}

}
