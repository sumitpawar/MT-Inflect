/**
 * 
 */
package com.jhu.cs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author sumit
 *
 */
public class Inflect {
	
	Map<String, Double> frmgram = new HashMap<String, Double>();
	Map<String, Double> lemgram = new HashMap<String, Double>();
	Map<String, Double> taggram = new HashMap<String, Double>();
	Map<String, Double> tregram = new HashMap<String, Double>();
	
	Map<String, Double> flgram = new HashMap<String, Double>();
	Map<String, Double> ftggram = new HashMap<String, Double>();
	Map<String, Double> ftrgram = new HashMap<String, Double>();
	Map<String, Double> ltggram = new HashMap<String, Double>();
	Map<String, Double> ltrgram = new HashMap<String, Double>();
	Map<String, Double> tgtrgram = new HashMap<String, Double>();
	Map<String, Double> ltgtrgram = new HashMap<String, Double>();
	Map<String, Double> fltgtrgram = new HashMap<String, Double>();
	
	Map<String, Set<String>> flmap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> lfmap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> ftgmap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> ltgmap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> tgfmap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> tglmap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> ltgtrfmap = new HashMap<String, Set<String>>();
	
	final String form = "/home/spawar/MT/inflect/data/train.form";
	final String lemma = "/home/spawar/MT/inflect/data/train.lemma";
	final String tag = "/home/spawar/MT/inflect/data/train.tag";
	final String tree = "/home/spawar/MT/inflect/data/train.tree";
	
	// dev-test
	final String lemmadev = "/home/spawar/MT/inflect/data/dtest.lemma";
	final String tagdev = "/home/spawar/MT/inflect/data/dtest.tag";
	final String treedev = "/home/spawar/MT/inflect/data/dtest.tree";
	
	final String BOS = "<s>";
	final String EOS = "</s>";
	
	private void updateMapping(Map<String, Set<String>> map, String key, String val) {
		Set<String> value = new HashSet<String>();
		if(map.containsKey(key)) {
			value = map.get(key);
		}
		value.add(val);
		map.put(key, value);
	}
	
	private void updateCount(Map<String, Double> gram, String key) {
		double cnt = 0;
		if(gram.containsKey(key)) {
			cnt = gram.get(key);
		}
		cnt++;
		gram.put(key, cnt);
	}
	
	private String getParentRoot(String[] tree, String node) {
		String parentRoot = "";
		String[] nodestruct = node.split("/");
		String nodeval = nodestruct[1];
		int nId = Integer.parseInt(nodestruct[0]);
		parentRoot = nodeval;
		if(nId != 0) {
			String pa = tree[nId - 1];
			String parr[] = pa.split("/");
			int paId = Integer.parseInt(parr[0]);
			String paVal = parr[1];
			parentRoot += "-" + paVal;
			if(paId != 0) {
				String rootVal = "";
				while(paId != 0) {
					String root[] = tree[paId - 1].split("/");
					paId = Integer.parseInt(root[0]);
					rootVal = root[1];
				}
				parentRoot += "-" + rootVal;
			}
		}
		return parentRoot;
	}
	
	public void loadData() {
		try{ 
			BufferedReader br1 = new BufferedReader(new FileReader(form));
			BufferedReader br2 = new BufferedReader(new FileReader(lemma));
			BufferedReader br3 = new BufferedReader(new FileReader(tag));
			BufferedReader br4 = new BufferedReader(new FileReader(tree));
			String sntnc = "", lem = "",
					tg = "", tre = "";
			
			while((sntnc = br1.readLine()) != null
					&& (lem = br2.readLine()) != null
					&& (tg = br3.readLine()) != null
					&& (tre = br4.readLine()) != null) {
				String[] farr = sntnc.split("\\s+");
				String[] larr = lem.split("\\s+");
				String[] tgarr = tg.split("\\s+");
				String[] trarr = tre.split("\\s+");
				int size = farr.length;
				for(int i = 0; i < size ; i++) {
					String fkey = farr[i];
					String lkey = larr[i];
					String tgkey = tgarr[i];
					String trekey = getParentRoot(trarr, trarr[i]);
					
					// update mapping
					updateMapping(flmap, fkey, lkey);
					updateMapping(lfmap, lkey, fkey);
					updateMapping(ftgmap, fkey, tgkey);
					updateMapping(ltgmap, lkey, tgkey);
					updateMapping(tgfmap, tgkey, fkey);
					updateMapping(tglmap, tgkey, lkey);
					updateMapping(ltgtrfmap, 
							(lkey+ "-" + tgkey + "-" + trekey), fkey);
					
					// unigram counts
					updateCount(frmgram, fkey);
					updateCount(lemgram, lkey);
					updateCount(taggram, tgkey);
					updateCount(tregram, trekey);

					
					updateCount(flgram, (fkey + "-" + lkey));
					updateCount(ftggram, (fkey + "-" + tgkey));
					updateCount(ftrgram, (fkey + "-" + trekey));
					updateCount(ltggram, (lkey + "-" + tgkey));
					updateCount(ltrgram, (lkey + "-" + trekey));
					updateCount(tgtrgram, (tgkey + "-" + trekey));
					updateCount(ltgtrgram, (lkey 
							+ "-" + tgkey + "-" + trekey));
					updateCount(fltgtrgram, (fkey + "-" + lkey 
							+ "-" + tgkey + "-" + trekey));
					
					
					// bigram counts
					String fkey2 = "";
					String lkey2 = "";
					String tgkey2 = "";
					if(i == 0) {
						fkey2 = lkey2 = tgkey2 = BOS;
					} else {
						fkey2 = farr[i - 1];
						lkey2 = larr[i - 1];
						tgkey2 = tgarr[i - 1];
					}
					updateCount(frmgram, (fkey2 + "-" + fkey) );
					updateCount(lemgram, (lkey2 + "-" + lkey) );
					updateCount(taggram, (tgkey2 + "-" + tgkey) );

					updateCount(flgram, (fkey + "-" + lkey2 + "-" + lkey));
					updateCount(flgram, (fkey2 + "-" + fkey + "-" + lkey));
					updateCount(flgram, (fkey2 + "-" + fkey + "-" + lkey2 + "-" + lkey));
					
					updateCount(ftggram, (fkey + "-" + tgkey2 + "-" + tgkey));
					updateCount(ftggram, (fkey2 + "-" + fkey + "-" + tgkey));
					updateCount(ftggram, (fkey2 + "-" + fkey + "-" + tgkey2 + "-" + tgkey));
					
					updateCount(ltggram, (lkey2 + "-" + lkey + "-" + tgkey));
					updateCount(ltggram, (lkey + "-" + tgkey2 + "-" + tgkey));
					updateCount(ltggram, (lkey2 + "-" + lkey + "-" + tgkey2 + "-" + tgkey));
					
					updateCount(fltgtrgram, (fkey2 + "-" + fkey + "-" 
							+ lkey2 + "-" + lkey 
							+ "-" + tgkey2 + "-" + tgkey));
					
					
					// trigram counts
					String fkey3 = "";
					String lkey3 = "";
					String tgkey3 = "";
					if(i > 0 && size >= 2) {
						if(i == 1) {
							fkey3 = lkey3 = tgkey3 = BOS;
						} else {
							fkey3 = farr[i - 2];
							lkey3 = larr[i - 2];
							tgkey3 = tgarr[i - 2];
						}
						updateCount(frmgram, (fkey3 + "-" + fkey2 + "-" + fkey) );
						updateCount(lemgram, (lkey3 + "-" + lkey2 + "-" + lkey) );
						updateCount(taggram, (tgkey3 + "-" + tgkey2 + "-" + tgkey) );
						
						updateCount(flgram, (fkey + "-" + lkey3 + "-" + lkey2 + "-" + lkey));
						updateCount(flgram, (fkey3 + "-" + fkey2 + "-" + fkey + "-" + lkey));
						updateCount(flgram, (fkey3 + "-" + fkey2 + "-" + fkey + "-" + 
															lkey3 + "-" + lkey2 + "-" + lkey));
						
						updateCount(ftggram, (fkey + "-" + tgkey3 + "-" + tgkey2 + "-" + tgkey));
						updateCount(ftggram, (fkey3 + "-" + fkey2 + "-" + fkey + "-" + tgkey));
						updateCount(ftggram, (fkey3 + "-" + fkey2 + "-" + fkey + "-" + 
															tgkey3 + "-" + tgkey2 + "-" + tgkey));
						
						updateCount(ltggram, (lkey3 + "-" + lkey2 + "-" + lkey + "-" + tgkey));
						updateCount(ltggram, (lkey + "-" + tgkey3 + "-" + tgkey2 + "-" + tgkey));
						updateCount(ltggram, (lkey3 + "-" + lkey2 + "-" + lkey + "-" + 
															tgkey3 + "-" + tgkey2 + "-" + tgkey));
						
						updateCount(fltgtrgram, (fkey3 + "-" + fkey2 + "-" + fkey + "-" +
								lkey3 + "-" + lkey2 + "-" + lkey + "-" + 
								tgkey3 + "-" + tgkey2 + "-" + tgkey));
					}
					
					// 4-gram counts
					if(i > 2 && size >= 3) {
						String fkey4 = "";
						String lkey4 = "";
						String tgkey4 = "";
						if(i == 2) {
							fkey4 = lkey4 = tgkey4 = BOS;
						} else {
							fkey4 = farr[i - 3];
							lkey4 = larr[i - 3];
							tgkey4 = tgarr[i - 3];
						}
						updateCount(frmgram, (fkey4 + "-" + fkey3 + "-" + fkey2 + "-" + fkey) );
						updateCount(lemgram, (lkey4 + "-" + lkey3 + "-" + lkey2 + "-" + lkey) );
						updateCount(taggram, (tgkey4 + "-" + tgkey3 + "-" + tgkey2 + "-" + tgkey) );

						updateCount(flgram, (fkey + "-" + lkey4 + "-" + lkey3 + "-" + lkey2 + "-" + lkey));
						updateCount(flgram, (fkey4 + "-" + fkey3 + "-" + fkey2 + "-" + fkey + "-" + lkey));
						updateCount(flgram, (fkey4 + "-" + fkey3 + "-" + fkey2 + "-" + fkey + "-" + 
															lkey4 + "-" + lkey3 + "-" + lkey2 + "-" + lkey));
						
						updateCount(ftggram, (fkey + "-" + tgkey4 + "-" + tgkey3 + "-" + tgkey2 + "-" + tgkey));
						updateCount(ftggram, (fkey4 + "-" + fkey3 + "-" + fkey2 + "-" + fkey + "-" + tgkey));
						updateCount(ftggram, (fkey4 + "-" + fkey3 + "-" + fkey2 + "-" + fkey + "-" + 
															tgkey4 + "-" + tgkey3 + "-" + tgkey2 + "-" + tgkey));
						
						updateCount(ltggram, (lkey4 + "-" + lkey3 + "-" + lkey2 + "-" + lkey + "-" + tgkey));
						updateCount(ltggram, (lkey + "-" + tgkey4 + "-" + tgkey3 + "-" + tgkey2 + "-" + tgkey));
						updateCount(ltggram, (lkey4 + "-" + lkey3 + "-" + lkey2 + "-" + lkey + "-" + 
															tgkey4 + "-" + tgkey3 + "-" + tgkey2 + "-" + tgkey));
						
						updateCount(fltgtrgram, (fkey4 + "-" + fkey3 + "-" + fkey2 + "-" + fkey + "-" +
								lkey4 + "-" + lkey3 + "-" + lkey2 + "-" + lkey + "-" + 
								tgkey4 + "-" + tgkey3 + "-" + tgkey2 + "-" + tgkey));
					}
				}
			}
			br1.close();
			br2.close();
			br3.close();
			br4.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: ");
			System.err.println(System.getProperty("user.dir"));
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("IOException: ");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void model1() {
		try{ 
//			BufferedReader br1 = new BufferedReader(new FileReader(form));
			BufferedReader br2 = new BufferedReader(new FileReader(lemmadev));
			BufferedReader br3 = new BufferedReader(new FileReader(tagdev));
			BufferedReader br4 = new BufferedReader(new FileReader(treedev));
			String /*sntnc = "",*/ lem = "",
					tg = "", tre = "";
			
			while(/*(sntnc = br1.readLine()) != null
					&&*/ (lem = br2.readLine()) != null
					&& (tg = br3.readLine()) != null
					&& (tre = br4.readLine()) != null) {
//				String[] farr = sntnc.split("\\s+");
				String[] larr = lem.split("\\s+");
				String[] tgarr = tg.split("\\s+");
				String[] trarr = tre.split("\\s+");
				int size = larr.length;
				for(int i = 0; i < size ; i++) {
					String lkey = larr[i];
					String tgkey = tgarr[i];
					String trekey = getParentRoot(trarr, trarr[i]);
					String key = lkey + "-" + tgkey + "-" + trekey;
					if(!ltgtrfmap.containsKey(key)) {
						System.out.println("Error: Not Found in DB for key: " + key);
//						return;
					}else {
						
					}
				}
			}
//			br1.close();
			br2.close();
			br3.close();
			br4.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: ");
			System.err.println(System.getProperty("user.dir"));
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("IOException: ");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void tagLemmaModel() {
		try{ 
			BufferedReader br1 = new BufferedReader(new FileReader(lemmadev));
			BufferedReader br2 = new BufferedReader(new FileReader(tagdev));
			String lem = "", tg = "";
			
			while((lem = br1.readLine()) != null
					&& (tg = br2.readLine()) != null) {
				String[] larr = lem.trim().split("\\s+");
				String[] tgarr = tg.trim().split("\\s+");
				
				int size = larr.length;
				String out  = "";
				for(int i = 0; i < size ; i++) {
					String lkey = larr[i];
					String tgkey = tgarr[i];
					double max = -1;
					String substr = "";
					Set<String> fs = new HashSet<String>(); 
					if(!tgfmap.containsKey(tgkey)) {
						fs.addAll(tgfmap.get(tgkey));
					} 
					if(lfmap.containsKey(lkey) ) {
						fs.addAll(lfmap.get(lkey));
					} else {
						String lkey2 = "";
						if(lkey.contains("_") && lkey.split("_").length > 0) {
							lkey2 = lkey.split("_")[0];
						}
						if(lfmap.containsKey(lkey2) ) {
							fs.addAll(lfmap.get(lkey2));
							lkey = lkey2;
						}
					}
					for(String f : fs) {
						double num = 0.0, den = 0.0;
						if(flgram.containsKey(f + "-" + lkey)) {
							num += flgram.get(f + "-" + lkey);
						}
						if(lemgram.containsKey(lkey)) {
							den += lemgram.get(lkey);
						} else  {
							den += frmgram.get(f);
						}
						if(ftggram.containsKey(f + "-" + tgkey)) {
							num += ftggram.get(f + "-" + tgkey);
						}
						if(taggram.containsKey(tgkey)) {
							den += taggram.get(tgkey);
						}else {
							den += frmgram.get(f);
						}
						if(ltggram.containsKey(lkey + "-" + tgkey)) {
							num += ltggram.get(lkey + "-" + tgkey);
						}
						if(lemgram.containsKey(lkey)) {
							den += lemgram.get(lkey);
						}
						if(taggram.containsKey(tgkey)) {
							den += taggram.get(tgkey);
						}
						if((num/den) > max) {
							max = num/den;
							substr = f;
						}
					}
					out += " " + substr;
				}
				System.out.println(out.trim());
			}
			br1.close();
			br2.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: ");
			System.err.println(System.getProperty("user.dir"));
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("IOException: ");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void lemmaTagModel() {
		try{ 
			BufferedReader br1 = new BufferedReader(new FileReader(lemmadev));
			BufferedReader br2 = new BufferedReader(new FileReader(tagdev));
			String lem = "", tg = "";
			
			while((lem = br1.readLine()) != null
					&& (tg = br2.readLine()) != null) {
				String[] larr = lem.trim().split("\\s+");
				String[] tgarr = tg.trim().split("\\s+");
				
				int size = larr.length;
				String out  = "";
				for(int i = 0; i < size ; i++) {
					String lkey = larr[i];
					String tgkey = tgarr[i];
					String lkey2 = "";
					double max = 0;
					if(lkey.contains("_") && lkey.split("_").length > 0) {
						lkey2 = lkey.split("_")[0];
					}
					if(!lfmap.containsKey(lkey) && !lfmap.containsKey(lkey2)) {
						//System.out.println("Error: Not Found in DB for key: " + lkey + " #### " + lkey2);
						out += " " + lkey2;
					}else {
						// No need to regularize because divisor lemma 'l' + tag 'tg' is same for all 
						Set<String> fs = lfmap.get(lkey);
						if(null == fs) {
							fs = lfmap.get(lkey2);
							lkey = lkey2;
						}
						String substr = "";
						double ltgval = 0.0;
						if(ltggram.containsKey(lkey + "-" + tgkey)) {
							ltgval += ltggram.get(lkey + "-" + tgkey);
						}
						for(String f : fs) {
							String flkey = f + "-" + lkey;
							if(flgram.containsKey(flkey)) {
								double val = (flgram.get(flkey)/lemgram.get(lkey)) 
													+ (ltgval/taggram.get(tgkey));
								if(ftggram.containsKey(f + "-" + tgkey)) {
									val += ftggram.get(f + "-" + tgkey)/frmgram.get(f);
								}
								if(val > max) {
									max = val;
									substr = f;
								}
							}else {
								if(substr.trim().isEmpty()) {
									substr = f;
								}
							}
						}
						out += " " + substr;
					}
				}
				System.out.println(out.trim());
			}
			br1.close();
			br2.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: ");
			System.err.println(System.getProperty("user.dir"));
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("IOException: ");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void lemmaModel() {
		try{ 
			BufferedReader br = new BufferedReader(new FileReader(lemmadev));
			String lem = "";
			
			while((lem = br.readLine()) != null) {
				String[] larr = lem.trim().split("\\s+");
				int size = larr.length;
				String out  = "";
				for(int i = 0; i < size ; i++) {
					String lkey = larr[i];
					String lkey2 = "";
					if(lkey.contains("_") && lkey.split("_").length > 0) {
						lkey2 = lkey.split("_")[0];
					}
					if(!lfmap.containsKey(lkey) && !lfmap.containsKey(lkey2)) {
						//System.out.println("Error: Not Found in DB for key: " + lkey + " #### " + lkey2);
						out += " " + lkey2;
					}else {
						// No need to regularize because divisor lemma 'l' is same for all 
						Set<String> fs = lfmap.get(lkey);
						if(null == fs) {
							fs = lfmap.get(lkey2);
						}
						String substr = "";
						double max = 0;
						for(String f : fs) {
							String flkey = f + "-" + lkey;
							if(flgram.containsKey(flkey)) {
								double val = flgram.get(flkey);
								if(val > max) {
									max = val;
									substr = f;
								}
							}else {
								if(substr.trim().isEmpty()) {
									substr = f;
								}
							}
						}
						out += " " + substr;
					}
				}
				System.out.println(out.trim());
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException: ");
			System.err.println(System.getProperty("user.dir"));
			e.printStackTrace();
		}catch (IOException e) {
			System.err.println("IOException: ");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long starttime = System.currentTimeMillis();
		Inflect inf  = new Inflect();
		inf.loadData();
		inf.lemmaTagModel();
		long endtime = System.currentTimeMillis();
		System.out.println("Total time to run: " + ((double) (endtime - starttime)/1000.00) + "s");
	}

}
