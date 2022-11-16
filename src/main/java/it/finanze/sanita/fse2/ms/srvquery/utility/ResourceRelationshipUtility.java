package it.finanze.sanita.fse2.ms.srvquery.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public class ResourceRelationshipUtility {

	public static void run(Bundle document) {
		Map<IdType, Set<IdType>> referencesMap = new HashMap<>();
		initialize(referencesMap, document);
		getResources(document).forEach(resource -> addReferences(referencesMap, resource));
		referencesMap.size();
	}

	private static void addReferences(Map<IdType, Set<IdType>> referencesMap, Resource resource) {
		List<Reference> references = new ArrayList<>();
		List<Object> responses = invokeAllGetters(resource);
		
		references.addAll(getObjectsOfType(responses, Reference.class));
		references.addAll(getListOfType(responses, Reference.class));
		
		List<IdType> idTypes = getIdTypes(references);
		referencesMap.get(resource.getIdElement()).addAll(idTypes);
		
//		responses.forEach(response -> addReferences(referencesMap, resource, response));
	}

	private static void addReferences(Map<IdType, Set<IdType>> referencesMap, Resource rootResource, Object resource) {
		if (resource instanceof Resource) return;
		
		List<Reference> references = new ArrayList<>();
		List<Object> responses = invokeAllGetters(resource);
		
		references.addAll(getObjectsOfType(responses, Reference.class));
		references.addAll(getListOfType(responses, Reference.class));
		
		List<IdType> idTypes = getIdTypes(references);
		referencesMap.get(rootResource.getIdElement()).addAll(idTypes);
		
		responses.forEach(response -> addReferences(referencesMap, rootResource, response));
	}

	private static void initialize(Map<IdType, Set<IdType>> referencesMap, Bundle document) {
		List<IdType> idTypes = getIdTypes(document);
		idTypes.forEach(idType -> referencesMap.put(idType, new HashSet<>()));
	}

	private static List<IdType> getIdTypes(Bundle document) {
		if (!document.hasEntry()) return new ArrayList<>();
		return document
				.getEntry()
				.stream()
				.map(entry -> entry.getResource())
				.map(Resource::getIdElement)
				.collect(Collectors.toList());
	}

	private static List<IdType> getIdTypes(List<Reference> references) {
		if (references == null) return new ArrayList<>();
		return references
				.stream()
				.filter(reference -> (reference.getResource() instanceof Resource))
				.map(reference -> Resource.class.cast(reference.getResource()))
				.map(Resource::getIdElement)
				.collect(Collectors.toList());
	}

	private static List<Resource> getResources(Bundle document) {
		if (!document.hasEntry()) return new ArrayList<>();
		return document
				.getEntry()
				.stream()
				.map(BundleEntryComponent::getResource)
				.collect(Collectors.toList());
	}

	private static List<Object> invokeAllGetters(Object resource) {
		Method[] methods = resource.getClass().getMethods();
		return Arrays
				.stream(methods)
				.filter(method -> method.getParameterTypes().length == 0)
				.map(method -> invoke(method, resource))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public static <T> List<T> getListOfType(List<Object> responses, Class<T> type) {
		return responses
				.stream()
				.filter(response -> List.class.isAssignableFrom(response.getClass()))
				.flatMap(response -> convertObjectToList(response, type).stream())
				.collect(Collectors.toList());
	}

	public static <T> List<T> getObjectsOfType(List<Object> responses, Class<T> type) {
		return responses
				.stream()
				.filter(response -> type.isAssignableFrom(response.getClass()))
				.map(response -> type.cast(response))
				.collect(Collectors.toList());
	}
	
	private static Object invoke(Method method, Object resource) {
		try {
			return method.invoke(resource);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static <T> List<T> convertObjectToList(Object obj, Class<T> returnType) {
	    return getList(obj)
	    		.stream()
	    		.filter(value -> value.getClass() == returnType)
	    		.map(value -> returnType.cast(value))
	    		.collect(Collectors.toList());
	}

	private static List<Object> getList(Object obj) {
	    if (obj.getClass().isArray()) 	return Arrays.asList((Object[])obj);
	    if (obj instanceof Collection) 	return new ArrayList<>((Collection<?>)obj);
	    return new ArrayList<>();
	}

}
