package org.eclipse.oomph.console.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.setup.Product;
import org.eclipse.oomph.setup.ProductVersion;
import org.eclipse.oomph.setup.SetupPackage;
import org.eclipse.oomph.setup.Stream;

@SuppressWarnings("restriction")
public class ProductVersionSelector {
	private final ResourceSet resourceSet;

	public ProductVersionSelector(ResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}

	public Product selectProduct(String productIdentifier) throws NotFoundException {
		List<Product> products = filter(SetupPackage.Literals.PRODUCT);
		Optional<Product> selectedProduct = products.stream().filter(a -> a.getName().equals(productIdentifier))
				.findFirst();
		if (selectedProduct.isPresent()) {
			return selectedProduct.get();
		}
		throw new NotFoundException("Cannot find the product called: " + productIdentifier + System.lineSeparator()
				+ "The available product(s) are: "
				+ products.stream().map(a -> a.getName() + ", ").distinct().reduce("", String::concat));
	}

	public List<Stream> selectStreams(List<String> arrayList) throws NotFoundException {
		List<Stream> streams = filter(SetupPackage.Literals.STREAM);
		List<Stream> result = streams.stream().distinct().filter(a -> arrayList.contains(a.getName()))
				.collect(Collectors.toList());
		if (!result.isEmpty() && !arrayList.isEmpty()) {
			return result;
		}

		throw new NotFoundException("Cannot find the streams: " + arrayList.toString() + System.lineSeparator()
				+ "The available stream(s) are: "
				+ streams.stream().map(a -> a.getName() + ", ").distinct().reduce("", String::concat));
	}

	public ProductVersion select(Product product, String productVersion) throws NotFoundException {
		Optional<ProductVersion> version = product.getVersions().stream()
				.filter(a -> a.getName().equals(productVersion)).findFirst();
		if (version.isPresent()) {
			return version.get();
		}
		throw new NotFoundException("Cannot find the ProductVersion called: " + productVersion + System.lineSeparator()
				+ "The available productversion(s) are: "
				+ product.getVersions().stream().map(a -> a.getName() + ", ").distinct().reduce("", String::concat));
	}

	public ProductVersion selectProductVersion(Product product) throws NotFoundException {
		return select(product, Parameters.VERSION);
	}

	@SuppressWarnings("unchecked")
	private <E> List<E> filter(EClass eClass) {
		List<E> result = new ArrayList<E>();
		TreeIterator<Notifier> iterator = EcoreUtil.getAllContents(resourceSet, true);

		while (iterator.hasNext()) {
			Notifier curr = iterator.next();
			if (eClass.isInstance(curr)) {
				result.add((E) curr);
			}
		}
		return result;

	}
}
