# Annotation composition

Annotation stats across all unique variants in 1000 Genomes Project dataset.

### gnomADe / gnomADg

Variants in the dataset annotated with gnomADe/gnomADg AF, % of total number of variants in chromosome

| Chromosome | Total Variants | gnomADe | % of Total (e) | gnomADg | % of Total (g) |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 11,122,108 | 444,169 | 3.9936% | 8,567,012 | 77.0269% |
| 2 | 11,346,898 | 320,910 | 2.8282% | 9,243,700 | 81.4646% |
| 3 | 9,174,723 | 246,010 | 2.6814% | 7,561,884 | 82.4208% |
| 4 | 9,052,888 | 180,522 | 1.9941% | 7,491,430 | 82.7518% |
| 5 | 8,360,596 | 198,460 | 2.3738% | 6,897,426 | 82.4992% |
| 6 | 7,769,430 | 216,742 | 2.7897% | 6,503,521 | 83.7065% |
| 7 | 7,650,658 | 242,376 | 3.1680% | 6,223,965 | 81.3520% |
| 8 | 7,107,721 | 173,080 | 2.4351% | 5,947,515 | 83.6768% |
| 9 | 6,060,229 | 199,515 | 3.2922% | 4,917,477 | 81.1434% |
| 10 | 6,528,421 | 184,841 | 2.8313% | 5,249,906 | 80.4162% |
| 11 | 6,497,949 | 263,776 | 4.0594% | 5,198,326 | 79.9995% |
| 12 | 6,274,676 | 231,387 | 3.6876% | 4,995,783 | 79.6182% |
| 13 | 4,964,954 | 84,100 | 1.6939% | 3,725,556 | 75.0371% |
| 14 | 4,207,249 | 159,422 | 3.7892% | 3,477,407 | 82.6528% |
| 15 | 3,942,335 | 180,869 | 4.5879% | 3,240,059 | 82.1863% |
| 16 | 4,391,676 | 233,058 | 5.3068% | 3,564,220 | 81.1585% |
| 17 | 3,910,272 | 262,250 | 6.7067% | 3,104,735 | 79.3995% |
| 18 | 3,826,115 | 73,891 | 1.9312% | 2,921,858 | 76.3662% |
| 19 | 3,020,915 | 299,424 | 9.9117% | 2,415,560 | 79.9612% |
| 20 | 3,193,659 | 111,774 | 3.4999% | 2,375,401 | 74.3787% |
| 21 | 2,021,390 | 58,109 | 2.8747% | 1,590,123 | 78.6648% |
| 22 | 2,092,942 | 124,030 | 5.9261% | 1,653,560 | 79.0065% |
| X | 5,311,865 | 107,483 | 2.0235% | 4,353,247 | 81.9533% |
| Y | 215,054 | 2,471 | 1.1490% | 115,142 | 53.5410% |
| **Total** | **138,044,723** | **4,598,669** | **3.3313%** | **111,334,813** | **80.6513%** |

---

Categorical annotations are preserved for all transcripts (for each variant), hence a single variant typically
carries several annotations in each category. Percentage of total variants in the dataset (138,044,723).

### [Clinical Significance (ClinVar="202502")](https://www.ncbi.nlm.nih.gov/clinvar/docs/clinsig/)

| Rank | Clinical Significance | Variants | Percentage |
| :--- | :--- | ---: | :--- |
| 1 | BENIGN | 193,150 | 0.139918% |
| 2 | LIKELY_BENIGN | 177,978 | 0.128928% |
| 3 | UNCERTAIN_SIGNIFICANCE | 160,704 | 0.116414% |
| 4 | PATHOGENIC | 3,394 | 0.002459% |
| 5 | LIKELY_PATHOGENIC | 2,694 | 0.001952% |
| 6 | NOT_PROVIDED | 1,921 | 0.001392% |
| 7 | RISK_FACTOR | 394 | 0.000285% |
| 8 | DRUG_RESPONSE | 313 | 0.000227% |
| 9 | OTHER | 296 | 0.000214% |
| 10 | ASSOCIATION | 265 | 0.000192% |
| 11 | AFFECTS | 80 | 0.000058% |
| 12 | PROTECTIVE | 72 | 0.000052% |
| 13 | LIKELY_RISK_ALLELE | 51 | 0.000037% |
| 14 | UNCERTAIN_RISK_ALLELE | 45 | 0.000033% |
| 15 | CONFERS_SENSITIVITY | 16 | 0.000012% |
| 16 | ESTABLISHED_RISK_ALLELE | 6 | 0.000004% |

### [AlphaMissense Class](https://www.science.org/doi/10.1126/science.adg7492)

| Rank | AlphaMissense Class | Variants | Percentage |
| :--- | :--- | ---: | :--- |
| 1 | LIKELY_BENIGN | 484,421 | 0.350916% |
| 2 | LIKELY_PATHOGENIC | 80,095 | 0.058021% |
| 3 | AMBIGUOUS | 53,397 | 0.038681% |

### [VEP Impact](https://www.ensembl.org/info/genome/variation/prediction/predicted_data.html?redirect=no)

| Rank | Impact Level | Variants | Percentage |
| :--- | :--- | ---: | :--- |
| 1 | MODIFIER | 118,165,321 | 85.599303% |
| 2 | LOW | 729,315 | 0.528318% |
| 3 | MODERATE | 723,849 | 0.524358% |
| 4 | HIGH | 76,012 | 0.055063% |

### [VEP Biotypes](https://www.ensembl.org/info/genome/genebuild/biotypes.html?redirect=no)

| Rank | Biotype | Variants | Percentage |
| :--- | :--- | ---: | :--- |
| 1 | PROTEIN_CODING | 52,800,136 | 38.248573% |
| 2 | LNCRNA | 43,495,168 | 31.508027% |
| 3 | SNRNA | 749,753 | 0.543123% |
| 4 | MIRNA | 664,014 | 0.481014% |
| 5 | TEC | 485,911 | 0.351995% |
| 6 | SNORNA | 287,778 | 0.208467% |
| 7 | RRNA | 19,082 | 0.013823% |
| 8 | RETAINED_INTRON | 9,672 | 0.007006% |
| 9 | PROCESSED_TRANSCRIPT | 6,328 | 0.004584% |
| 10 | IG_PSEUDOGENE | 466 | 0.000338% |

### [VEP Variant Type](https://www.ensembl.org/info/genome/variation/prediction/classification.html?redirect=no#classes)

| Rank | Variant Type | Variants | Percentage |
| :--- | :--- | ---: | :--- |
| 1 | SNV | 109,755,527 | 79.507224% |
| 2 | DELETION | 5,978,708 | 4.330994% |
| 3 | INSERTION | 2,962,074 | 2.145735% |

### VEP Feature Type

| Rank | Feature Type | Variants | Percentage |
| :--- | :--- | ---: | :--- |
| 1 | TRANSCRIPT | 87,579,003 | 63.442485% |
| 2 | REGULATORYFEATURE | 5,815,334 | 4.212645% |

### [VEP Consequence](https://www.ensembl.org/info/genome/variation/prediction/predicted_data.html?redirect=no#consequences)

| Rank | Consequence | Variants | Percentage |
| :--- | :--- | ---: | :--- |
| 1 | INTRON_VARIANT | 74,551,507 | 54.005329% |
| 2 | NON_CODING_TRANSCRIPT_VARIANT | 33,459,217 | 24.237954% |
| 3 | INTERGENIC_VARIANT | 31,117,306 | 22.541467% |
| 4 | DOWNSTREAM_GENE_VARIANT | 17,947,741 | 13.001396% |
| 5 | UPSTREAM_GENE_VARIANT | 16,923,454 | 12.259399% |
| 6 | REGULATORY_REGION_VARIANT | 5,815,334 | 4.212645% |
| 7 | NON_CODING_TRANSCRIPT_EXON_VARIANT | 2,835,680 | 2.054175% |
| 8 | MISSENSE_VARIANT | 710,029 | 0.514347% |
| 9 | SYNONYMOUS_VARIANT | 436,013 | 0.315849% |
| 10 | SPLICE_POLYPYRIMIDINE_TRACT_VARIANT | 187,959 | 0.136158% |
| 11 | SPLICE_REGION_VARIANT | 170,772 | 0.123708% |
| 12 | SPLICE_DONOR_REGION_VARIANT | 31,943 | 0.023140% |
| 13 | FRAMESHIFT_VARIANT | 21,906 | 0.015869% |
| 14 | SPLICE_DONOR_VARIANT | 19,501 | 0.014127% |
| 15 | STOP_GAINED | 16,800 | 0.012170% |
| 16 | SPLICE_ACCEPTOR_VARIANT | 15,093 | 0.010933% |
| 17 | SPLICE_DONOR_5TH_BASE_VARIANT | 13,713 | 0.009934% |
| 18 | INFRAME_DELETION | 9,812 | 0.007108% |
| 19 | INFRAME_INSERTION | 3,946 | 0.002858% |
| 20 | START_LOST | 2,199 | 0.001593% |
| 21 | MATURE_MIRNA_VARIANT | 2,176 | 0.001576% |
| 22 | STOP_LOST | 1,197 | 0.000867% |
| 23 | CODING_SEQUENCE_VARIANT | 837 | 0.000606% |
| 24 | STOP_RETAINED_VARIANT | 758 | 0.000549% |
| 25 | PROTEIN_ALTERING_VARIANT | 165 | 0.000120% |
| 26 | REGULATORY_REGION_ABLATION | 160 | 0.000116% |
| 27 | START_RETAINED_VARIANT | 84 | 0.000061% |
| 28 | INCOMPLETE_TERMINAL_CODON_VARIANT | 46 | 0.000033% |
| 29 | TRANSCRIPT_ABLATION | 4 | 0.000003% |
